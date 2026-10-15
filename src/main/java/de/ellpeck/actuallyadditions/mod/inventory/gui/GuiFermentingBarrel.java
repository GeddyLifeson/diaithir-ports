/*
 * This file ("GuiFermentingBarrel.java") is part of the Actually Additions mod for Minecraft.
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
import de.ellpeck.actuallyadditions.mod.inventory.ContainerFermentingBarrel;
import de.ellpeck.actuallyadditions.mod.tile.TileEntityFermentingBarrel;
import de.ellpeck.actuallyadditions.mod.util.AssetUtil;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

import javax.annotation.Nonnull;



public class GuiFermentingBarrel extends AAScreen<ContainerFermentingBarrel> {

    private static final Identifier RES_LOC = AssetUtil.getGuiLocation("gui_fermenting_barrel");
    private final TileEntityFermentingBarrel press;
    private FluidDisplay input;
    private FluidDisplay output;

    public GuiFermentingBarrel(ContainerFermentingBarrel container, Inventory inventory, Component title) {
        super(container, inventory, title, 176, 93 + 86);
        this.press = container.barrel;
    }

    @Override
    public void extractRenderState(@Nonnull GuiGraphicsExtractor guiGraphics, int x, int y, float f) {
        super.extractRenderState(guiGraphics, x, y, f);
        this.input.render(guiGraphics, x, y);
        this.output.render(guiGraphics, x, y);
    }

    @Override
    public void init() {
        super.init();
        this.input = new FluidDisplay(this.leftPos + 60, this.topPos + 5, this.press.tanks.inputTank);
        this.output = new FluidDisplay(this.leftPos + 98, this.topPos + 5, this.press.tanks.outputTank);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int x, int y, float f) {
        super.extractBackground(guiGraphics, x, y, f);

        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, AssetUtil.GUI_INVENTORY_LOCATION, this.leftPos, this.topPos + 93, 0, 0, 176, 86, 256, 256);

        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, RES_LOC, this.leftPos, this.topPos, 0, 0, 176, 93, 256, 256);

        if (this.press.currentProcessTime > 0) {
            int i = this.press.getProcessScaled(29);
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, RES_LOC, this.leftPos + 82, this.topPos + 34, 176, 0, 12, i, 256, 256);
        }

        this.input.draw(guiGraphics);
        this.output.draw(guiGraphics);
    }
}
