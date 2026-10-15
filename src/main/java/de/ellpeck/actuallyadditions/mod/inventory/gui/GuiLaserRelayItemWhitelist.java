/*
 * This file ("GuiLaserRelayItemWhitelist.java") is part of the Actually Additions mod for Minecraft.
 * It is created and owned by Ellpeck and distributed
 * under the Actually Additions License to be found at
 * http://ellpeck.de/actaddlicense
 * View the source code at https://github.com/Ellpeck/ActuallyAdditions
 *
 * © 2015-2017 Ellpeck
 */

package de.ellpeck.actuallyadditions.mod.inventory.gui;

import net.minecraft.client.renderer.RenderPipelines;

import com.mojang.blaze3d.systems.RenderSystem;
import de.ellpeck.actuallyadditions.mod.inventory.ContainerLaserRelayItemWhitelist;
import de.ellpeck.actuallyadditions.mod.network.PacketHelperClient;
import de.ellpeck.actuallyadditions.mod.tile.TileEntityLaserRelayItemAdvanced;
import de.ellpeck.actuallyadditions.mod.util.AssetUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;


public class GuiLaserRelayItemWhitelist extends AAScreen<ContainerLaserRelayItemWhitelist> {

    private static final Identifier RES_LOC = AssetUtil.getGuiLocation("gui_laser_relay_item_whitelist");
    private final Component inboundText = Component.translatable("info.actuallyadditions.gui.inbound");
    private final Component outboundText = Component.translatable("info.actuallyadditions.gui.outbound");
    private final TileEntityLaserRelayItemAdvanced tile;

    private FilterSettingsGui leftFilter;
    private FilterSettingsGui rightFilter;

    private Button buttonSmartWhitelistLeft;
    private Button buttonSmartWhitelistRight;

    public GuiLaserRelayItemWhitelist(ContainerLaserRelayItemWhitelist container, Inventory inventory, Component title) {
        super(container, inventory, title, 176, 93 + 86);
        this.tile = container.tile;
    }

    @Override
    public void containerTick() {
        super.containerTick();

        this.leftFilter.tick();
        this.rightFilter.tick();
    }

    @Override
    public void init() {
        super.init();

        this.leftFilter = new FilterSettingsGui(this.tile.leftFilter, this.leftPos + 3, this.topPos + 6, true, this::addRenderableWidget, id -> PacketHelperClient.sendButtonPacket(this.tile, id),  0);
        this.rightFilter = new FilterSettingsGui(this.tile.rightFilter, this.leftPos + 157, this.topPos + 6, true, this::addRenderableWidget, id -> PacketHelperClient.sendButtonPacket(this.tile, id),  4);

        this.buttonSmartWhitelistLeft = this.addRenderableWidget(Button.builder(
                        Component.literal("S"),
                        (button) ->
                                PacketHelperClient.sendButtonPacket(this.tile, 8))
                .bounds(this.leftPos + 3, this.topPos + 79, 16, 16)
                .build());

        this.buttonSmartWhitelistRight = this.addRenderableWidget(Button.builder(
                        Component.literal("S"),
                        (button) ->
                                PacketHelperClient.sendButtonPacket(this.tile, 9))
                .bounds(this.leftPos + 157, this.topPos + 79, 16, 16)
                .build());
    }

    @Override
    public void extractRenderState(@Nonnull GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTicks);
        this.leftFilter.drawHover(guiGraphics, mouseX, mouseY);
        this.rightFilter.drawHover(guiGraphics, mouseX, mouseY);

        if (this.buttonSmartWhitelistLeft.isMouseOver(mouseX, mouseY) || this.buttonSmartWhitelistRight.isMouseOver(mouseX, mouseY)) {
            List<FormattedCharSequence> list = new ArrayList<>();
            list.add(Component.translatable("info.actuallyadditions.gui.smart").withStyle(ChatFormatting.BOLD).getVisualOrderText());
            list.addAll(this.font.split(Component.translatable("info.actuallyadditions.gui.smartInfo"), 200));
            guiGraphics.setTooltipForNextFrame(this.font, list, mouseX, mouseY); //renderTooltip
        }
    }

    @Override
    public void extractLabels(@Nonnull GuiGraphicsExtractor guiGraphics, int x, int y) {
        AssetUtil.displayNameString(guiGraphics, this.font, this.imageWidth, -10, this.title.getString());

        guiGraphics.text(font, inboundText, 46 - this.font.width(inboundText) / 2, 80, 0xFF404040, false);
        guiGraphics.text(font, outboundText, 131 - this.font.width(outboundText) / 2, 80, 0xFF404040, false);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int x, int y, float f) {
        super.extractBackground(guiGraphics, x, y, f);

        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, AssetUtil.GUI_INVENTORY_LOCATION, this.leftPos, this.topPos + 93, 0, 0, 176, 86, 256, 256);

        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, RES_LOC, this.leftPos, this.topPos, 0, 0, 176, 93, 256, 256);
    }
}
