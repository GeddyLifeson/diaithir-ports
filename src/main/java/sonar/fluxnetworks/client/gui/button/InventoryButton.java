package sonar.fluxnetworks.client.gui.button;

import sonar.fluxnetworks.client.gui.basic.GuiFocusable;

import net.minecraft.client.renderer.RenderPipelines;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import sonar.fluxnetworks.FluxNetworks;
import sonar.fluxnetworks.api.network.WirelessType;
import sonar.fluxnetworks.client.gui.basic.GuiButtonCore;
import sonar.fluxnetworks.client.gui.tab.GuiTabWireless;
import sonar.fluxnetworks.common.util.FluxUtils;

import javax.annotation.Nonnull;

public class InventoryButton extends GuiButtonCore {

    public static final Identifier INVENTORY = FluxNetworks.location(
            "textures/gui/inventory_configuration.png");

    public WirelessType mType;
    private final int mU0;
    private final int mV0;
    public GuiTabWireless mHost;
    private final String mText;

    public InventoryButton(GuiTabWireless screen, int x, int y, int width, int height, @Nonnull WirelessType type,
                           int u0, int v0) {
        super(screen, x, y, width, height);
        mHost = screen;
        mType = type;
        mText = type.getTranslatedName();
        mU0 = u0;
        mV0 = v0;
    }

    @Override
    protected void drawButton(GuiGraphicsExtractor gr, int mouseX, int mouseY, float deltaTicks) {
        int color = mHost.getNetwork().getNetworkColor() | 0xFF000000;
        GuiFocusable.setColor(FluxUtils.getRed(color), FluxUtils.getGreen(color), FluxUtils.getBlue(color), 1.0f);
        boolean hovered = isMouseHovered(mouseX, mouseY);

        gr.blit(RenderPipelines.GUI_TEXTURED, INVENTORY, x, y, mU0, mV0 + height * (mType.isActivated(mHost.mWirelessMode) ? 1 : 0), width, height, 256, 256);

        if (hovered) {
            Font font = screen.getMinecraft().font;
            gr.text(font, mText, x + (width - font.width(mText)) / 2 + 1, y - 9, 0xFFFFFFFF);
        }
    }
}
