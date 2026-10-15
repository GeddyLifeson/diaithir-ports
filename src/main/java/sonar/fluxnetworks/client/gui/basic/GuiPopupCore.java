package sonar.fluxnetworks.client.gui.basic;

import net.minecraft.client.input.MouseButtonEvent;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import org.lwjgl.glfw.GLFW;
import sonar.fluxnetworks.common.util.FluxUtils;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public abstract class GuiPopupCore<T extends GuiFluxCore> extends GuiFocusable {

    protected final List<GuiButtonCore> mButtons = new ArrayList<>();

    public final T mHost;

    protected float mAlpha = 0;

    public GuiPopupCore(@Nonnull T host) {
        super(host.getMenu(), host.mPlayer);
        mHost = host;
    }

    public void init() {
        super.init();
        mButtons.clear();
    }

    @Override
    public void onClose() {
        mButtons.clear();
    }

    @Override
    public final void extractRenderState(@Nonnull GuiGraphicsExtractor gr, int mouseX, int mouseY, float deltaTicks) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void extractLabels(@Nonnull GuiGraphicsExtractor gr, int mouseX, int mouseY) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final void extractBackground(@Nonnull GuiGraphicsExtractor gr, int mouseX, int mouseY, float deltaTicks) {
        throw new UnsupportedOperationException();
    }

    public void drawForegroundLayer(@Nonnull GuiGraphicsExtractor gr, int mouseX, int mouseY, float deltaTicks) {
        for (GuiButtonCore button : mButtons) {
            button.drawButton(gr, mouseX, mouseY, deltaTicks);
        }
    }

    public void drawBackgroundLayer(@Nonnull GuiGraphicsExtractor gr, int mouseX, int mouseY, float deltaTicks) {
        mAlpha = Math.min(1.0f, mAlpha + deltaTicks / 6); // animation duration is (1000/20*6)=300ms

        GuiFocusable.setColor(1.0f, 1.0f, 1.0f, mAlpha);
        GuiFocusable.setTexture(BACKGROUND);
        blitBackgroundOrFrame(gr);

        int color = mHost.getNetwork().getNetworkColor() | 0xFF000000;
        GuiFocusable.setColor(FluxUtils.getRed(color), FluxUtils.getGreen(color), FluxUtils.getBlue(color), mAlpha);
        GuiFocusable.setTexture(FRAME);
        blitBackgroundOrFrame(gr);

        // dimmer
        int bgColor = (int) (mAlpha * 128) << 24;
        gr.fill(0, 0, width, height, bgColor);

        for (Renderable widget : renderables) {
            widget.extractRenderState(gr, mouseX, mouseY, deltaTicks);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        boolean result = false;
        for (GuiButtonCore button : mButtons) {
            if (button.mClickable && button.isMouseHovered(event.x(), event.y())) {
                onButtonClicked(button, (int) event.x(), (int) event.y(), event.button());
                result = true;
                break;
            }
        }
        for (GuiEventListener child : this.children()) {
            if (child.mouseClicked(event, doubleClick)) {
                setFocused(child);
                if (event.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                    setDragging(true);
                }
                return true;
            }
        }
        boolean focused = false;
        for (GuiEventListener child : this.children()) {
            if (child instanceof EditBox editBox && editBox.isFocused() &&
                    editBox.isMouseOver(event.x(), event.y())) {
                focused = true;
                break;
            }
        }
        if (!focused) {
            setFocused(null);
            return true;
        }
        return result;
    }

    public void onButtonClicked(GuiButtonCore button, int mouseX, int mouseY, int mouseButton) {
    }
}
