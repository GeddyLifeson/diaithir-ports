package io.github.tr100000.researcher.screen;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import io.github.tr100000.researcher.ClientResearchTracker;
import io.github.tr100000.researcher.Research;
import io.github.tr100000.researcher.Researcher;
import io.github.tr100000.researcher.config.ResearcherClientConfig;
import io.github.tr100000.researcher.config.ResearcherConfigs;
import io.github.tr100000.researcher.graph.GraphLayout;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.neoforged.fml.loading.FMLLoader;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.util.Mth;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class ResearchTreeView extends ResearchNodeContainingView {
    private static final float[] ZOOM_LEVELS = new float[] {
            1.0f, 0.84f, 0.71f, 0.59f, 0.5f
    };

    private GraphLayout.RenderedGraph renderedGraph = GraphLayout.RenderedGraph.EMPTY;
    private final BiMap<GraphLayout.RenderedNode, ResearchNodeWidget> renderedNodeWidgets = HashBiMap.create();
    private final BiMap<Research, ResearchNodeWidget> nodeWidgets = HashBiMap.create();
    private final Multimap<ResearchNodeWidget, ResearchNodeWidget> successorConnections = HashMultimap.create();
    private final Multimap<ResearchNodeWidget, ResearchNodeWidget> predecessorConnections = HashMultimap.create();
    private final List<ResearchNodeWidget> topNodes = new ObjectArrayList<>();
    private final List<ResearchNodeWidget> bottomNodes = new ObjectArrayList<>();

    private @Nullable ResearchNodeWidget highlightLocked = null;
    private boolean isScrollable;
    private MinMaxBounds.Ints horizontalScrollBounds = MinMaxBounds.Ints.ANY;
    private MinMaxBounds.Ints verticalScrollBounds = MinMaxBounds.Ints.ANY;
    private double offsetX;
    private double offsetY;
    private int highlightColor;

    private int zoomLevel = 0;
    private float scale = 1.0f;

    private final ClientResearchTracker researchTracker;

    private static final int CONNECTION_COLOR = 0xFFAAAAAA;

    public ResearchTreeView(ResearchScreen parent, int width, int height) {
        super(parent, ResearchScreen.getSidebarWidth(), 0, width, height);
        this.researchTracker = parent.researchManager;
        this.scissorRect = new ScreenRectangle(x, y, width, height);
    }

    public void initWith(Research research) {
        clearChildren();
        highlightLocked = null;
        horizontalScrollBounds = MinMaxBounds.Ints.ANY;
        verticalScrollBounds = MinMaxBounds.Ints.ANY;
        offsetX = 0;
        offsetY = 0;
        highlightColor = ResearcherConfigs.client.highlightColor.get();

        resetZoom();

        final int nodeSize = 48;
        final int centeredNodeSize = 64;

        long layoutStartTime = System.currentTimeMillis();

        GraphLayout graphLayout = new GraphLayout(researchTracker.getGraph());
        Map<Research, Integer> researchDepthMap;

        switch (ResearcherConfigs.client.researchTreeMode.get()) {
            case DIRECTLY_RELATED:
                researchDepthMap = new Object2IntOpenHashMap<>();
                researchTracker.directPredecessorsOf(research).forEach(r -> researchDepthMap.put(r, -1));
                researchDepthMap.put(research, 0);
                researchTracker.directSuccessorsOf(research).forEach(r -> researchDepthMap.put(r, 1));
                break;
            case ALL_RELATED:
                researchDepthMap = researchTracker.getRelatedMap(research, researchTracker::getDepthMapPredecessors, ResearcherConfigs.client.discoveryResearchMode.get() ? researchTracker::getResearchableSuccessors : researchTracker::getDepthMapSuccessors);
                break;
            case ALL:
                researchDepthMap = graphLayout.createNodeLayerMap();
                break;
            default:
                researchDepthMap = new Object2IntOpenHashMap<>();
                break;
        }

        GraphLayout.Settings settings = new GraphLayout.Settings(research, nodeSize, centeredNodeSize, 2, 4, 8, 4, 4);
        renderedGraph = graphLayout.render(researchDepthMap, settings);

        for (GraphLayout.RenderedNode node : renderedGraph.nodes()) {
            Research r = node.researchOrThrow();
            ResearchNodeWidget widget = new ResearchNodeWidget(parent, this, node.x() - node.width() / 2, node.y() - node.height() / 2, node.width(), node.height(), node.layer(), r);
            nodeWidgets.put(r, widget);
            renderedNodeWidgets.put(node, widget);
            addDrawableChild(widget);

            if (ResearcherConfigs.client.researchTreeMode.get() == ResearcherClientConfig.ResearchTreeMode.DIRECTLY_RELATED) {
                (researchDepthMap.get(r) < 0 ? topNodes : bottomNodes).add(widget);
            }
        }

        for (GraphLayout.RenderedEdge edge : renderedGraph.edges()) {
            ResearchNodeWidget from = renderedNodeWidgets.get(edge.from());
            ResearchNodeWidget to = renderedNodeWidgets.get(edge.to());
            successorConnections.put(from, to);
            predecessorConnections.put(to, from);
        }

        GraphLayout.RenderedNode centerNode = renderedGraph.centeredNode();
        if (centerNode != null) {
            offsetX = width / 2.0 - centerNode.x();
            offsetY = height / 2.0 - centerNode.y();
        }

        if (!FMLLoader.getCurrent().isProduction())
            Researcher.LOGGER.info("Rendered graph with {} nodes and {} edges ({}ms)", renderedGraph.nodes().size(), renderedGraph.edges().size(), System.currentTimeMillis() - layoutStartTime);

        onResize();
    }

    @Override
    public void onResize() {
        this.x = ResearchScreen.getSidebarWidth();
        this.width = parent.width - ResearchScreen.getSidebarWidth();
        this.height = parent.height;
        this.scissorRect = new ScreenRectangle(x, y, width, height);

        ScreenRectangle rect = rectWithPadding(getContentsRect(), 16);
        float rectScreenWidth = rect.width() * scale;
        float rectScreenHeight = rect.height() * scale;
        float rectScreenLeft = rect.left() * scale;
        float rectScreenTop = rect.top() * scale;
        float rectScreenRight = rect.right() * scale;
        float rectScreenBottom = rect.bottom() * scale;

        isScrollable = false;
        if (rectScreenWidth > width) {
            horizontalScrollBounds = MinMaxBounds.Ints.between((int)(width - rectScreenRight), (int)-rectScreenLeft);
            isScrollable = true;
        }
        else {
            horizontalScrollBounds = MinMaxBounds.Ints.exactly((int)(width / 2.0f - (rectScreenLeft + rectScreenRight) / 2));
        }
        if (rectScreenHeight > height) {
            verticalScrollBounds = MinMaxBounds.Ints.between((int)(height - rectScreenBottom), (int)-rectScreenTop);
            isScrollable = true;
        }
        else {
            verticalScrollBounds = MinMaxBounds.Ints.exactly((int)(height / 2.0f - (rectScreenTop + rectScreenBottom) / 2));
        }
        enforceScrollBounds();
    }

    @Override
    public ScreenRectangle getContentsRect() {
        return renderedGraph.allNodes().stream()
                .map(n -> new ScreenRectangle(n.x() - n.width() / 2, n.y() - n.height() / 2, n.width(), n.height()))
                .reduce(AbstractResearchView::combineRects)
                .orElseGet(ScreenRectangle::empty);
    }

    private GraphLayout.@Nullable RenderedEdge getRenderedEdge(ResearchNodeWidget from, ResearchNodeWidget to) {
        for (GraphLayout.RenderedEdge c : renderedGraph.edges()) {
            if (c.matches(from.research, to.research)) return c;
        }
        return null;
    }

    @Override
    public void extractRenderState(final GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        graphics.pose().pushMatrix();
        assert scissorRect != null;
        graphics.enableScissor(scissorRect.left(), scissorRect.top(), scissorRect.right(), scissorRect.bottom());
        graphics.pose().translate(getOffsetX(), getOffsetY());
        graphics.pose().scale(scale);

        int newMouseX = (int)toOffsetX(mouseX);
        int newMouseY = (int)toOffsetY(mouseY);

        ResearchNodeWidget hovered = highlightLocked != null ? highlightLocked : getCurrentHovered(graphics, newMouseX, newMouseY);
        extractConnections(graphics, hovered);
        if (hovered != null) {
            highlightConnected(graphics, hovered);
        }

        super.extractView(graphics, newMouseX, newMouseY, delta);

        graphics.disableScissor();
        graphics.pose().popMatrix();
    }

    private @Nullable ResearchNodeWidget getCurrentHovered(final GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        if (!graphics.containsPointInScissor((int)offsetToScreenX(mouseX), (int)offsetToScreenY(mouseY))) return null;

        for (ResearchNodeWidget node : nodeWidgets.values()) {
            if (node.isMouseOver(mouseX, mouseY)) {
                return node;
            }
        }
        return null;
    }

    private void extractConnections(final GuiGraphicsExtractor graphics, @Nullable ResearchNodeWidget currentHovered) {
        extractFakeConnections(graphics, currentHovered);

        renderedGraph.edges().forEach(edge -> extractConnection(graphics, edge, CONNECTION_COLOR));

        if (currentHovered != null) {
            successorConnections.get(currentHovered).forEach(node -> extractConnection(graphics, currentHovered, node, highlightColor));
            predecessorConnections.get(currentHovered).forEach(node -> extractConnection(graphics, node, currentHovered, highlightColor));
        }
    }

    private void extractConnection(final GuiGraphicsExtractor graphics, ResearchNodeWidget from, ResearchNodeWidget to, int color) {
        GraphLayout.RenderedEdge edge = getRenderedEdge(from, to);
        if (edge == null) return;
        extractConnection(graphics, edge, color);
    }

    private void extractConnection(final GuiGraphicsExtractor graphics, GraphLayout.RenderedEdge edge, int color) {
        for (GraphLayout.RenderedEdgeSegment segment : edge.segments()) {
            graphics.verticalLine(segment.fromX(), segment.fromY(), segment.midY(), color); // down
            graphics.horizontalLine(segment.fromX(), segment.toX(), segment.midY(), color); // right
            graphics.verticalLine(segment.toX(), segment.midY(), segment.toY(), color); // down
        }
    }

    private void highlightConnected(final GuiGraphicsExtractor graphics, ResearchNodeWidget node) {
        successorConnections.get(node).forEach(successor -> extractHighlight(graphics, successor, 1));
        predecessorConnections.get(node).forEach(predecessor -> extractHighlight(graphics, predecessor, 1));
        extractHighlight(graphics, node, highlightLocked != null ? 2 : 1);
    }

    private void extractFakeConnections(final GuiGraphicsExtractor graphics, @Nullable ResearchNodeWidget currentHovered) {
        final int length = 20;

        for (ResearchNodeWidget node : topNodes) {
            int x = node.getX() + node.getWidth() / 2;
            int startY = node.getY();
            int endY = startY - length;
            extractFakeConnection(graphics, x, startY, endY, node == currentHovered ? highlightColor : CONNECTION_COLOR);
        }

        for (ResearchNodeWidget node : bottomNodes) {
            int x = node.getX() + node.getWidth() / 2;
            int startY = node.getY() + node.getHeight();
            int endY = startY + length;
            extractFakeConnection(graphics, x, startY, endY, node == currentHovered ? highlightColor : CONNECTION_COLOR);
        }
    }

    private void extractFakeConnection(final GuiGraphicsExtractor graphics, int x, int startY, int endY, int color) {
        final int segmentCount = 3;
        final int height = endY - startY;
        final int segmentHeight = height / segmentCount;

        // Draw segments
        for (int i = 0; i < segmentCount; i += 2) {
            int segmentStartY = startY + segmentHeight * i;
            int segmentEndY = (int)(segmentStartY + (segmentHeight + 1.25F));
            graphics.verticalLine(x, segmentStartY, segmentEndY, color);
        }
    }

    private void extractHighlight(final GuiGraphicsExtractor graphics, ResearchNodeWidget node, int size) {
        for (int i = 1; i <= size; i++) {
            graphics.outline(node.getX() - i, node.getY() - i, node.getWidth() + i * 2, node.getHeight() + i * 2, highlightColor);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
        boolean mouseClicked = super.mouseClicked(event, doubled);
        if (!mouseClicked && highlightLocked != null) {
            highlightLocked = null;
            return true;
        }
        return mouseClicked;
    }

    @Override
    public void onNodeShiftClicked(ResearchNodeWidget widget) {
        highlightLocked = widget;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        if (event.button() == 0) {
            this.offsetX += dx;
            this.offsetY += dy;
            enforceScrollBounds();
            return true;
        }
        else {
            return super.mouseDragged(event, dx, dy);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        offsetX += horizontalAmount * ResearcherConfigs.client.researchTreeScrollSensitivity.get();
        offsetY += verticalAmount * ResearcherConfigs.client.researchTreeScrollSensitivity.get();
        enforceScrollBounds();
        return true;
    }

    private void enforceScrollBounds() {
        offsetX = Math.clamp(offsetX, horizontalScrollBounds.min().orElse(0), horizontalScrollBounds.max().orElse(0));
        offsetY = Math.clamp(offsetY, verticalScrollBounds.min().orElse(0), verticalScrollBounds.max().orElse(0));
    }

    @Override
    public boolean isScrollable() {
        return isScrollable;
    }

    @Override
    public int getOffsetX() {
        return (int)offsetX + getX();
    }

    @Override
    public int getOffsetY() {
        return (int)offsetY + getY();
    }

    @Override
    public double toOffsetX(double x) {
        return (x - getOffsetX()) / scale;
    }

    @Override
    public double toOffsetY(double y) {
        return (y - getOffsetY()) / scale;
    }

    @Override
    public double offsetToScreenX(double offsetX) {
        return getOffsetX() + offsetX * scale;
    }

    @Override
    public double offsetToScreenY(double offsetY) {
        return getOffsetY() + offsetY * scale;
    }

    public void zoomIn() {
        setZoomLevel(zoomLevel - 1);
    }

    public void zoomOut() {
        setZoomLevel(zoomLevel + 1);
    }

    private void setZoomLevel(int zoomLevel) {
        int oldZoomLevel = this.zoomLevel;
        this.zoomLevel = Mth.clamp(zoomLevel, 0, ZOOM_LEVELS.length - 1);
        if (oldZoomLevel != this.zoomLevel) {
            scale = ZOOM_LEVELS[zoomLevel];
            onResize();
        }
    }

    public void resetZoom() {
        setZoomLevel(0);
    }

    public boolean canZoomIn() {
        return zoomLevel > 0;
    }

    public boolean canZoomOut() {
        return zoomLevel < ZOOM_LEVELS.length - 1;
    }

    @Override
    public float getScale() {
        return scale;
    }

    @Override
    public void clearChildren() {
        super.clearChildren();
        renderedGraph = GraphLayout.RenderedGraph.EMPTY;
        renderedNodeWidgets.clear();
        nodeWidgets.clear();
        successorConnections.clear();
        predecessorConnections.clear();
        topNodes.clear();
        bottomNodes.clear();
    }
}
