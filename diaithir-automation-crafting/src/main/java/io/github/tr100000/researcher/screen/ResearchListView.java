package io.github.tr100000.researcher.screen;

import com.google.common.base.Predicates;
import io.github.tr100000.researcher.Research;
import io.github.tr100000.researcher.config.ResearcherConfigs;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ResearchListView extends ResearchNodeContainingView {
    private static final int BACKGROUND_COLOR = 0xFF202020;
    private static final int BORDER_COLOR = 0xFF000000;

    private final List<ResearchNodeWidget> researchWidgets = new ObjectArrayList<>();
    private final Map<ResearchNodeWidget, String> researchTitles = new Object2ObjectOpenHashMap<>();
    private final EditBox searchField;

    private boolean isScrollable;
    private MinMaxBounds.Ints scrollBounds = MinMaxBounds.Ints.ANY;
    private double offsetY;

    public ResearchListView(ResearchScreen parent, int height) {
        super(parent, 0, ResearchScreen.getInfoViewHeight(), ResearchScreen.getSidebarWidth(), height);

        searchField = new EditBox(client.font, 4, 4, ResearchScreen.getSidebarWidth() - 8, 20, Component.translatable("screen.researcher.search"));
        searchField.setResponder(this::searchAndReposition);

        List<Research> researchList = new ObjectArrayList<>(parent.researchManager.listAll());
        researchList.sort(Research.statusComparator(parent.researchManager).thenComparing(Research.idComparator(parent.researchManager)));
        for (Research research : researchList) {
            if (ResearcherConfigs.client.discoveryResearchMode.get() && !parent.researchManager.isAvailableOrFinished(research)) {
                continue;
            }

            ResearchNodeWidget widget = new ResearchNodeWidget(parent, this, 0, 0, 30, 30, research);
            researchWidgets.add(widget);
            researchTitles.put(widget, research.getTitle(parent.researchManager).getString().toUpperCase());
        }

        onResize();
    }

    public void searchAndReposition(String text) {
        Predicate<CharSequence> predicate = Predicates.containsPattern(text.toUpperCase());

        int x = 4;
        int y = 29;
        for (ResearchNodeWidget widget : researchWidgets) {
            if (predicate.test(researchTitles.get(widget))) {
                widget.setPosition(x, y);

                x += 32;
                if (x > getWidth() - 30) {
                    x = 4;
                    y += 32;
                }
            } else {
                widget.setPosition(-100, -100);
            }
        }

        offsetY = 0;
        ScreenRectangle rect = rectWithPadding(getContentsRect(), 4);
        if (rect.height() > getHeight()) {
            scrollBounds = MinMaxBounds.Ints.between(-rect.bottom() + getHeight(), -rect.top());
            isScrollable = true;
        }
        else {
            scrollBounds = MinMaxBounds.Ints.exactly(0);
            isScrollable = false;
        }
    }

    @Override
    public void onResize() {
        clearChildren();
        this.y = ResearchScreen.getInfoViewHeight();
        this.width = ResearchScreen.getSidebarWidth();
        this.height = parent.height - ResearchScreen.getInfoViewHeight();
        this.scissorRect = new ScreenRectangle(0, y, width, height);

        searchField.setWidth(ResearchScreen.getSidebarWidth() - 8);
        addDrawableChild(searchField);
        researchWidgets.forEach(this::addDrawableChild);

        searchAndReposition(searchField.getValue());
    }

    @Override
    public ScreenRectangle getContentsRect() {
        return children().stream()
                .filter(LayoutElement.class::isInstance)
                .filter(g -> ((LayoutElement) g).getX() > 0)
                .map(GuiEventListener::getRectangle)
                .reduce(AbstractResearchView::combineRects)
                .orElseGet(ScreenRectangle::empty);
    }

    @Override
    public void visitWidgets(Consumer<AbstractWidget> consumer) {
        children().forEach(child -> {
            if (child instanceof AbstractWidget widgetChild && widgetChild.getX() > 0) {
                consumer.accept(widgetChild);
            }
        });
    }

    @Override
    public void extractView(final GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        graphics.fill(0, getY() - 1, getWidth(), parent.height, BACKGROUND_COLOR);
        graphics.outline(0, getY() - 1, getWidth(), parent.height, BORDER_COLOR);

        graphics.pose().translate(getOffsetX(), getOffsetY());

        int newMouseX = (int)toOffsetX(mouseX);
        int newMouseY = (int)toOffsetY(mouseY);

        super.extractView(graphics, newMouseX, newMouseY, delta);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        offsetY += verticalAmount * ResearcherConfigs.client.researchTreeScrollSensitivity.get();
        offsetY = Math.clamp(offsetY, scrollBounds.min().orElse(0), scrollBounds.max().orElse(0));
        return true;
    }

    @Override
    public boolean isScrollable() {
        return isScrollable;
    }

    @Override
    public int getOffsetY() {
        return (int)offsetY + getY();
    }
}
