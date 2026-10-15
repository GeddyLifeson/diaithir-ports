/*
 * This file ("GuiFluidCollector.java") is part of the Actually Additions mod for Minecraft.
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
import de.ellpeck.actuallyadditions.mod.inventory.ContainerFluidCollector;
import de.ellpeck.actuallyadditions.mod.tile.TileEntityFluidCollector;
import de.ellpeck.actuallyadditions.mod.util.AssetUtil;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

import javax.annotation.Nonnull;



public class GuiFluidCollector extends AAScreen<ContainerFluidCollector> {

    private static final Identifier RES_LOC = AssetUtil.getGuiLocation("gui_fluid_collector");
    private final TileEntityFluidCollector collector;
    private FluidDisplay fluid;

    public GuiFluidCollector(ContainerFluidCollector container, Inventory inventory, Component title) {
        super(container, inventory, title, 176, 93 + 86);
        this.collector = container.collector;
    }

    @Override
    public void extractRenderState(@Nonnull GuiGraphicsExtractor guiGraphics, int x, int y, float f) {
        super.extractRenderState(guiGraphics, x, y, f);

        this.fluid.render(guiGraphics, x, y);
    }

    @Override
    public void init() {
        super.init();
        this.fluid = new FluidDisplay(this.leftPos + 67, this.topPos + 5, this.collector.tank);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int x, int y, float f) {
        super.extractBackground(guiGraphics, x, y, f);

        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, AssetUtil.GUI_INVENTORY_LOCATION, this.leftPos, this.topPos + 93, 0, 0, 176, 86, 256, 256);

        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, RES_LOC, this.leftPos, this.topPos, 0, 0, 176, 93, 256, 256);

        this.fluid.draw(guiGraphics);
    }
}
