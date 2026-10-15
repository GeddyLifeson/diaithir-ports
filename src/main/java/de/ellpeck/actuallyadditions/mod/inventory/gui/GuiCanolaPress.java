/*
 * This file ("GuiCanolaPress.java") is part of the Actually Additions mod for Minecraft.
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
import de.ellpeck.actuallyadditions.mod.inventory.ContainerCanolaPress;
import de.ellpeck.actuallyadditions.mod.tile.TileEntityCanolaPress;
import de.ellpeck.actuallyadditions.mod.util.AssetUtil;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

import javax.annotation.Nonnull;


public class GuiCanolaPress extends AAScreen<ContainerCanolaPress> {

    private static final Identifier RES_LOC = AssetUtil.getGuiLocation("gui_canola_press");
    private final TileEntityCanolaPress press;
    private EnergyDisplay energy;
    private FluidDisplay fluid;

    public GuiCanolaPress(ContainerCanolaPress container, Inventory inventory, Component title) {
        super(container, inventory, title, 176, 93 + 86);
        this.press = container.press;
    }

    @Override
    public void init() {
        super.init();
        this.energy = new EnergyDisplay(this.leftPos + 42, this.topPos + 5, this.press.storage);
        this.fluid = new FluidDisplay(this.leftPos + 116, this.topPos + 5, this.press.tank);
    }

    @Override
    public void extractRenderState(@Nonnull GuiGraphicsExtractor guiGraphics, int x, int y, float f) {
        super.extractRenderState(guiGraphics, x, y, f);

        this.energy.render(guiGraphics, x, y);
        this.fluid.render(guiGraphics, x, y);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int x, int y, float f) {
        super.extractBackground(guiGraphics, x, y, f);

        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, AssetUtil.GUI_INVENTORY_LOCATION, this.leftPos, this.topPos + 93, 0, 0, 176, 86, 256, 256);

        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, RES_LOC, this.leftPos, this.topPos, 0, 0, 176, 93, 256, 256);

        if (this.press.currentProcessTime > 0) {
            int i = this.press.getProcessScaled(29);
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, RES_LOC, this.leftPos + 83, this.topPos + 32, 176, 0, 12, i, 256, 256);
        }

        this.energy.draw(guiGraphics);
        this.fluid.draw(guiGraphics);
    }
}
