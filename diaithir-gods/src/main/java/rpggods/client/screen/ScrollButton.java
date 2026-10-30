package rpggods.client.screen;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class ScrollButton<T extends Screen> extends RGButton {

    /** A consumer to handle when the scroll ends **/
    private final Consumer<ScrollButton<T>> scrollEndHandler;
    /** Amount scrolled (0.0 = top, 1.0 = bottom) **/
    private float scrollAmount;
    /** If the scroll bar is enabled **/
    private final Predicate<T> enabled;

    private final T screen;
    private final Identifier texture;
    private final int u;
    private final int v;
    private final boolean vertical;
    private final int uWidth;
    private final int vHeight;

    private boolean dragging;

    public ScrollButton(final T gui, final int x, final int y,
                        final int width, final int height, final int uX, final int vY,
                        final Identifier textureIn, final boolean isVertical, final Predicate<T> isEnabled,
                        final Consumer<ScrollButton<T>> onScrollEnd) {
        super(x, y, width, height, Component.empty(), b -> {
        });
        screen = gui;
        u = uX;
        v = vY;
        texture = textureIn;
        vertical = isVertical;
        if (isVertical) {
            uWidth = 12;
            vHeight = 15;
        } else {
            uWidth = 15;
            vHeight = 12;
        }
        enabled = isEnabled;
        scrollEndHandler = onScrollEnd;
        scrollAmount = 0;
        scrollEndHandler.accept(this);
    }

    @Override
    protected void draw(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTicks) {
        final boolean isEnabled = enabled.test(screen);
        final float scroll = isEnabled ? scrollAmount : 0.0F;
        final int vOffset = isEnabled ? 0 : vHeight;
        final int offset = Mth.clamp((int) (scroll * this.height - this.vHeight / 2), 1, this.height - vHeight - 1);
        final int dx = vertical ? 1 : offset;
        final int dy = vertical ? offset : 1;
        GuiUtil.blit(g, texture, getX() + dx, getY() + dy, u, v + vOffset, uWidth, vHeight);
    }

    @Override
    public void onClick(final MouseButtonEvent event, boolean doubleClick) {
        if (enabled.test(screen)) {
            updateScrollAmount(event.x(), event.y());
            scrollEndHandler.accept(this);
        }
    }

    @Override
    protected void onDrag(final MouseButtonEvent event, final double dragX, final double dragY) {
        if (enabled.test(screen)) {
            dragging = true;
            updateScrollAmount(event.x(), event.y());
        }
    }

    @Override
    public void onRelease(final MouseButtonEvent event) {
        if (enabled.test(screen) && dragging) {
            updateScrollAmount(event.x(), event.y());
            scrollEndHandler.accept(this);
        }
        dragging = false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (enabled.test(screen)) {
            scrollAmount = Mth.clamp((float) (scrollAmount - scrollY), 0.0F, 1.0F);
            scrollEndHandler.accept(this);
            return true;
        }
        return false;
    }

    private void updateScrollAmount(final double mouseX, final double mouseY) {
        if (vertical) {
            scrollAmount = Mth.clamp((float) (mouseY - this.getY()) / (float) this.height, 0.0F, 1.0F);
        } else {
            scrollAmount = Mth.clamp((float) (mouseX - this.getX()) / (float) this.width, 0.0F, 1.0F);
        }
    }

    public float getScrollAmount() {
        return scrollAmount;
    }

    public void resetScroll() {
        scrollAmount = 0.0F;
        scrollEndHandler.accept(this);
    }
}
