/*
 * This file ("GuiRangedCollector.java") is part of the Actually Additions mod for Minecraft.
 * It is created and owned by Ellpeck and distributed
 * under the Actually Additions License to be found at
 * http://ellpeck.de/actaddlicense
 * View the source code at https://github.com/Ellpeck/ActuallyAdditions
 *
 * © 2015-2017 Ellpeck
 */

package de.ellpeck.actuallyadditions.mod.inventory.gui;

import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import net.minecraft.client.renderer.RenderPipelines;

import com.mojang.blaze3d.systems.RenderSystem;
import de.ellpeck.actuallyadditions.mod.inventory.ContainerRangedCollector;
import de.ellpeck.actuallyadditions.mod.network.packet.ButtonToTilePacket;
import de.ellpeck.actuallyadditions.mod.tile.TileEntityRangedCollector;
import de.ellpeck.actuallyadditions.mod.util.AssetUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

import javax.annotation.Nonnull;


public class GuiRangedCollector extends AAScreen<ContainerRangedCollector> {

    private static final Identifier RES_LOC = AssetUtil.getGuiLocation("gui_ranged_collector");
    private final TileEntityRangedCollector collector;

    private FilterSettingsGui filter;

    public GuiRangedCollector(ContainerRangedCollector container, Inventory inventory, Component title) {
        super(container, inventory, title, 176, 86 + 86);
        this.collector = container.collector;
    }

    @Override
    public void init() {
        super.init();

        this.filter = new FilterSettingsGui(this.collector.filter, this.leftPos + 3, this.topPos + 6, true, this::addRenderableWidget, this::buttonClicked, 0);
    }

    @Override
    public void extractRenderState(@Nonnull GuiGraphicsExtractor guiGraphics, int x, int y, float f) {
        super.extractRenderState(guiGraphics, x, y, f);

        this.filter.drawHover(guiGraphics, x, y);
    }

    @Override
    public void containerTick() {
        super.containerTick();

        this.filter.tick();
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int x, int y, float f) {
        super.extractBackground(guiGraphics, x, y, f);

        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, AssetUtil.GUI_INVENTORY_LOCATION, this.leftPos, this.topPos + 86, 0, 0, 176, 86, 256, 256);

        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, RES_LOC, this.leftPos, this.topPos, 0, 0, 176, 86, 256, 256);
    }
    
    public void buttonClicked(int id) {
        ClientPacketDistributor.sendToServer(new ButtonToTilePacket(collector.getLevel().dimension().identifier(),
                this.collector.getBlockPos(), Minecraft.getInstance().player.getId(), id));
    }

}
