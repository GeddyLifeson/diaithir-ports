package io.github.tr100000.researcher.screen;

import io.github.tr100000.trutils.api.gui.AbstractView;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.input.MouseButtonEvent;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

public abstract class AbstractResearchView extends AbstractView implements ScrollableView, LayoutElement {
    protected static final Minecraft client = Minecraft.getInstance();

    protected @Nullable ScreenRectangle scissorRect;
    protected final ResearchScreen parent;
    protected int x;
    protected int y;
    protected int width;
    protected int height;

    protected AbstractResearchView(ResearchScreen parent, int x, int y, int width, int height) {
        this.parent = parent;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    @Override
    public void setX(int x) {
        this.x = x;
    }

    @Override
    public void setY(int y) {
        this.y = y;
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    public void onResize() {}

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= getX() && mouseY >= getY() && mouseX < getX() + getWidth() && mouseY < getY() + getHeight();
    }

    @Override
    public ScreenRectangle getRectangle() {
        return LayoutElement.super.getRectangle();
    }

    @Override
    public void visitWidgets(Consumer<AbstractWidget> consumer) {
        children().forEach(child -> {
            if (child instanceof AbstractWidget widgetChild) {
                consumer.accept(widgetChild);
            }
        });
    }

    @Override
    public void extractRenderState(final GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        graphics.pose().pushMatrix();
        if (scissorRect != null)
            graphics.enableScissor(scissorRect.left(), scissorRect.top(), scissorRect.right(), scissorRect.bottom());
        extractView(graphics, mouseX, mouseY, delta);
        if (scissorRect != null)
            graphics.disableScissor();
        graphics.pose().popMatrix();
    }

    public void extractView(final GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
    }

    public abstract ScreenRectangle getContentsRect();

    protected static ScreenRectangle combineRects(ScreenRectangle a, ScreenRectangle b) {
        int xMin = Math.min(a.left(), b.left());
        int xMax = Math.max(a.right(), b.right());
        int yMin = Math.min(a.top(), b.top());
        int yMax = Math.max(a.bottom(), b.bottom());
        return new ScreenRectangle(xMin, yMin, xMax - xMin, yMax - yMin);
    }

    protected static ScreenRectangle rectWithPadding(ScreenRectangle rect, int padding) {
        return new ScreenRectangle(rect.left() - padding, rect.top() - padding, rect.width() + padding * 2, rect.height() + padding * 2);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        super.mouseMoved(toOffsetX(mouseX), toOffsetY(mouseY));
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
        return super.mouseClicked(new MouseButtonEvent(toOffsetX(event.x()), toOffsetY(event.y()), event.buttonInfo()), doubled);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        return super.mouseReleased(new MouseButtonEvent(toOffsetX(event.x()), toOffsetY(event.y()), event.buttonInfo()));
    }
}
