package sonar.fluxnetworks.client.gui.basic;

import net.minecraft.client.input.CharacterEvent;

import net.minecraft.client.input.MouseButtonEvent;

import net.minecraft.client.input.KeyEvent;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.world.entity.player.Player;
import org.lwjgl.glfw.GLFW;
import sonar.fluxnetworks.common.connection.FluxMenu;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class GuiPopupHost extends GuiFocusable {

    private GuiPopupCore<?> mCurrentPopup;

    protected GuiPopupHost(@Nonnull FluxMenu menu, @Nonnull Player player) {
        super(menu, player);
    }

    //// OPEN POP UP \\\\

    public final void openPopup(GuiPopupCore<?> popup) {
        if (popup == null || popup.mHost != this) {
            return;
        }
        closePopup();
        mCurrentPopup = popup;
        mCurrentPopup.init(width, height);
        onPopupOpen(mCurrentPopup);
    }

    protected void onPopupOpen(GuiPopupCore<?> popup) {
    }

    //// CLOSE POP UP \\\\\

    public final void closePopup() {
        if (mCurrentPopup != null) {
            onPopupClose(mCurrentPopup);
            mCurrentPopup.onClose();
            mCurrentPopup = null;
        }
    }

    // used for obtaining info from popups
    protected void onPopupClose(GuiPopupCore<?> popup) {
    }

    @Nullable
    public final GuiPopupCore<?> getCurrentPopup() {
        return mCurrentPopup;
    }

    //// mouse moved \\\\

    @Override
    public final void mouseMoved(double mouseX, double mouseY) {
        if (mCurrentPopup != null) {
            mCurrentPopup.mouseMoved(mouseX, mouseY);
            return;
        }
        if (onMouseMoved(mouseX, mouseY)) {
            return;
        }
        super.mouseMoved(mouseX, mouseY);
    }

    protected boolean onMouseMoved(double mouseX, double mouseY) {
        return false;
    }

    //// mouse click \\\\

    @Override
    public final boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (mCurrentPopup != null) {
            return mCurrentPopup.mouseClicked(event, doubleClick);
        }
        boolean result = onMouseClicked(event.x(), event.y(), event.button());
        for (GuiEventListener child : children()) {
            if (child.mouseClicked(event, doubleClick)) {
                setFocused(child);
                if (event.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                    setDragging(true);
                }
                return true;
            }
        }
        boolean focused = false;
        for (GuiEventListener child : children()) {
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

    protected boolean onMouseClicked(double mouseX, double mouseY, int mouseButton) {
        return false;
    }

    //// mouse release \\\\

    @Override
    public final boolean mouseReleased(MouseButtonEvent event) {
        if (mCurrentPopup != null) {
            return mCurrentPopup.mouseReleased(event);
        }
        return onMouseReleased(event.x(), event.y(), event.button()) ||
                super.mouseReleased(event);
    }

    public boolean onMouseReleased(double mouseX, double mouseY, int mouseButton) {
        return false;
    }

    //// mouse dragged \\\\

    @Override
    public final boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
        if (mCurrentPopup != null) {
            return mCurrentPopup.mouseDragged(event, deltaX, deltaY);
        }
        return onMouseDragged(event.x(), event.y(), event.button(), deltaX, deltaY) ||
                super.mouseDragged(event, deltaX, deltaY);
    }

    public boolean onMouseDragged(double mouseX, double mouseY, int mouseButton, double deltaX, double deltaY) {
        return false;
    }

    //// mouse scrolled \\\\

    @Override
    public final boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (mCurrentPopup != null) {
            return mCurrentPopup.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        }
        return onMouseScrolled(mouseX, mouseY, scrollX, scrollY) ||
                super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    public boolean onMouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return false;
    }

    //// key pressed \\\\

    @Override
    public final boolean keyPressed(KeyEvent event) {
        if (mCurrentPopup != null) {
            return mCurrentPopup.keyPressed(event);
        }
        return onKeyPressed(event.key(), event.scancode(), event.modifiers()) ||
                super.keyPressed(event);
    }

    public boolean onKeyPressed(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    //// key released \\\\

    @Override
    public final boolean keyReleased(KeyEvent event) {
        if (mCurrentPopup != null) {
            return mCurrentPopup.keyReleased(event);
        }
        return onKeyReleased(event.key(), event.scancode(), event.modifiers()) ||
                super.keyReleased(event);
    }

    public boolean onKeyReleased(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    //// char typed \\\\

    @Override
    public final boolean charTyped(CharacterEvent event) {
        if (mCurrentPopup != null) {
            return mCurrentPopup.charTyped(event);
        }
        return onCharTypes((char) event.codepoint(), 0) ||
                super.charTyped(event);
    }

    public boolean onCharTypes(char c, int modifiers) {
        return false;
    }

    //// INIT \\\\

    @Override
    public void init() {
        super.init();
        if (mCurrentPopup != null) {
            mCurrentPopup.init(width, height);
        }
    }

    protected void drawForegroundLayer(GuiGraphicsExtractor gr, int mouseX, int mouseY, float deltaTicks) {
    }

    protected void drawBackgroundLayer(GuiGraphicsExtractor gr, int mouseX, int mouseY, float deltaTicks) {
    }

    @Override
    public final void extractRenderState(@Nonnull GuiGraphicsExtractor gr, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(gr, mouseX, mouseY, partialTick);
        // The last parameter should be partialTick, but there was a bug in Vanilla that passed it
        // as deltaTicks, NeoForge fixed it. But we do need deltaTicks instead of partialTick...
        float deltaTicks = getMinecraft().getDeltaTracker().getRealtimeDeltaTicks();
        drawForegroundLayer(gr, mouseX, mouseY, deltaTicks);

        if (mCurrentPopup != null) {
            gr.pose().pushMatrix();
            gr.pose().translate(0, 0);
            mCurrentPopup.drawBackgroundLayer(gr, mouseX, mouseY, deltaTicks);
            gr.pose().translate(0, 0);
            mCurrentPopup.drawForegroundLayer(gr, mouseX, mouseY, deltaTicks);
            gr.pose().popMatrix();
        }
    }

    @Override
    protected final void extractLabels(@Nonnull GuiGraphicsExtractor gr, int mouseX, int mouseY) {
    }

    @Override
    public final void extractBackground(@Nonnull GuiGraphicsExtractor gr, int mouseX, int mouseY, float partialTick) {
        float deltaTicks = getMinecraft().getDeltaTracker().getRealtimeDeltaTicks();
        drawBackgroundLayer(gr, mouseX, mouseY, deltaTicks);
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (mCurrentPopup != null) {
            mCurrentPopup.containerTick();
        }
    }
}
