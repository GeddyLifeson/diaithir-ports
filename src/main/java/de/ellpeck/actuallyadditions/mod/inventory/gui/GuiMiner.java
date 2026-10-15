/*
 * This file ("GuiMiner.java") is part of the Actually Additions mod for Minecraft.
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
import de.ellpeck.actuallyadditions.mod.inventory.ContainerMiner;
import de.ellpeck.actuallyadditions.mod.network.PacketHelperClient;
import de.ellpeck.actuallyadditions.mod.tile.TileEntityVerticalDigger;
import de.ellpeck.actuallyadditions.mod.util.AssetUtil;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;



public class GuiMiner extends AAScreen<ContainerMiner> {

    private static final Identifier RES_LOC = AssetUtil.getGuiLocation("gui_breaker");
    private final TileEntityVerticalDigger miner;

    public GuiMiner(ContainerMiner container, Inventory inventory, Component title) {
        super(container, inventory, title, 176, 93 + 86);
        this.miner = container.miner;
    }

    @Override
    public void init() {
        super.init();

        this.addRenderableWidget(Button.builder(Component.literal("Mode"), $ -> {
            PacketHelperClient.sendButtonPacket(this.miner, 0);
        }).bounds(this.leftPos + this.imageWidth / 2 - 51, this.topPos + 75, 50, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("Reset"), $ -> {
            PacketHelperClient.sendButtonPacket(this.miner, 1);
        }).bounds(this.leftPos + this.imageWidth / 2 + 1, this.topPos + 75, 50, 20).build());
    }
    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int x, int y, float f) {
        super.extractBackground(guiGraphics, x, y, f);

        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, AssetUtil.GUI_INVENTORY_LOCATION, this.leftPos, this.topPos + 93, 0, 0, 176, 86, 256, 256);

        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, RES_LOC, this.leftPos, this.topPos, 0, 0, 176, 93, 256, 256);

        String mining = this.miner.onlyMineOres
            ? "Only Mining Ores"
            : "Mining Everything";
        guiGraphics.text(font, mining, this.leftPos + this.imageWidth / 2 - this.font.width(mining) / 2, this.topPos + 8, 0xFF404040, false);
    }
}
