package sonar.fluxnetworks.client.gui.basic;

import net.minecraft.client.input.KeyEvent;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.player.Player;
import org.lwjgl.glfw.GLFW;
import sonar.fluxnetworks.FluxNetworks;
import sonar.fluxnetworks.client.gui.EnumNavigationTab;
import sonar.fluxnetworks.client.gui.button.FluxEditBox;
import sonar.fluxnetworks.common.connection.FluxMenu;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Fixes the focus of popup host and dialog popup.
 */
public abstract class GuiFocusable extends AbstractContainerScreen<FluxMenu> {

    public static final int TEXTURE_SIZE = 512;

    public static final Identifier BACKGROUND = Identifier.fromNamespaceAndPath(
            FluxNetworks.MODID, "textures/gui/gui_background.png");
    public static final Identifier FRAME = Identifier.fromNamespaceAndPath(
            FluxNetworks.MODID, "textures/gui/gui_frame.png");
    public static final Identifier ICON = Identifier.fromNamespaceAndPath(
            FluxNetworks.MODID, "textures/gui/gui_icon.png");

    public GuiFocusable(FluxMenu menu, @Nonnull Player player) {
        super(menu, player.getInventory(), CommonComponents.EMPTY);
    }

    /**
     * Un-focus other text elements
     */
    @Override
    public void setFocused(@Nullable GuiEventListener listener) {
        super.setFocused(listener);
        for (GuiEventListener child : children()) {
            if (child != listener && child instanceof FluxEditBox editBox) {
                if (editBox.isFocused()) {
                    editBox.setFocused(false);
                }
            }
        }
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        InputConstants.Key key = InputConstants.getKey(event);
        if (getFocused() != null) {
            if (event.key() == GLFW.GLFW_KEY_ESCAPE ||
                    event.key() == GLFW.GLFW_KEY_ENTER ||
                    event.key() == GLFW.GLFW_KEY_KP_ENTER) {
                setFocused(null);
                return true;
            }
            if (getMinecraft().options.keyInventory.isActiveAndMatches(key)) {
                // consume the event if there's a focus
                return true;
            }
        } else if (event.key() == GLFW.GLFW_KEY_ESCAPE || getMinecraft().options.keyInventory.isActiveAndMatches(key)) {
            if (this instanceof GuiPopupCore<?> core) {
                core.mHost.closePopup();
                return true;
            }
            if (this instanceof GuiTabCore core) {
                if (core.getNavigationTab() == EnumNavigationTab.TAB_HOME) {
                    onClose();
                } else {
                    core.switchTab(EnumNavigationTab.TAB_HOME, true);
                }
            }
            return true;
        }
        boolean result = super.keyPressed(event);
        // consume the event if there's a focus
        return result || getFocused() != null;
    }

    @Override
    protected void containerTick() {
        super.containerTick();
//        for (GuiEventListener child : children()) {
//            if (child instanceof FluxEditBox editBox) {
//                editBox.tick();
//            }
//        }
    }

    // ponytail: stands in for the global shader texture and colour 26.1's GUI dropped. The colour applies to the next
    // blit only, because the port removed the callers' resets to white.
    private static Identifier sTexture = ICON;
    private static int sColor = -1;

    public static void setTexture(Identifier texture) {
        sTexture = texture;
    }

    public static void setColor(float r, float g, float b, float a) {
        sColor = ARGB.colorFromFloat(a, r, g, b);
    }

    protected void blitBackgroundOrFrame(@Nonnull GuiGraphicsExtractor gr) {
        gr.pose().pushMatrix();
        gr.pose().translate(width / 2f - 86, height / 2f + 5 - 86);
        gr.blit(RenderPipelines.GUI_TEXTURED, sTexture, 0, 0, 0, 0, 172, 172, 1, 1, 1, 1, sColor);
        gr.pose().popMatrix();
        sColor = -1;
    }

    public static void blitF(@Nonnull GuiGraphicsExtractor gr, float x, float y, float width, float height,
                             float uOffset, float vOffset, float uWidth, float vHeight) {
        gr.pose().pushMatrix();
        gr.pose().translate(x, y);
        gr.pose().scale(width / uWidth, height / vHeight);
        gr.blit(RenderPipelines.GUI_TEXTURED, sTexture, 0, 0, uOffset, vOffset, (int) uWidth, (int) vHeight,
                (int) uWidth, (int) vHeight, TEXTURE_SIZE, TEXTURE_SIZE, sColor);
        gr.pose().popMatrix();
        sColor = -1;
    }
}
