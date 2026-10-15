/*
 * This file ("GuiOilGenerator.java") is part of the Actually Additions mod for Minecraft.
 * It is created and owned by Ellpeck and distributed
 * under the Actually Additions License to be found at
 * http://ellpeck.de/actaddlicense
 * View the source code at https://github.com/Ellpeck/ActuallyAdditions
 *
 * © 2015-2017 Ellpeck
 */

package de.ellpeck.actuallyadditions.mod.inventory.gui;

import org.joml.Matrix3x2fStack;

import net.minecraft.client.renderer.RenderPipelines;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import de.ellpeck.actuallyadditions.mod.inventory.ContainerOilGenerator;
import de.ellpeck.actuallyadditions.mod.tile.TileEntityOilGenerator;
import de.ellpeck.actuallyadditions.mod.util.AssetUtil;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

import javax.annotation.Nonnull;



public class GuiOilGenerator extends AAScreen<ContainerOilGenerator> {

    private static final Identifier RES_LOC = AssetUtil.getGuiLocation("gui_oil_generator");
    private final TileEntityOilGenerator generator;

    private EnergyDisplay energy;
    private FluidDisplay fluid;

    public GuiOilGenerator(ContainerOilGenerator container, Inventory inventory, Component title) {
        super(container, inventory, title, 176, 93 + 86);
        this.generator = container.generator;
    }

    @Override
    public void init() {
        super.init();
        this.energy = new EnergyDisplay(this.leftPos + 42, this.topPos + 5, this.generator.storage);
        this.fluid = new FluidDisplay(this.leftPos + 116, this.topPos + 5, this.generator.tank);
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

        if (this.generator.currentBurnTime > 0 && this.generator.maxBurnTime > 0) {
            int i = this.generator.getBurningScaled(13);
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, RES_LOC, this.leftPos + 72, this.topPos + 44 + 12 - i, 176, 96 - i, 14, i, 256, 256);
        }

        if (this.generator.maxBurnTime > 0 && this.generator.currentEnergyProduce > 0) {
            guiGraphics.centeredText(this.font, Component.literal(this.generator.currentEnergyProduce + " ").append(Component.translatable("misc.actuallyadditions.energy_tick")), this.leftPos + 87, this.topPos + 65, 0xFFFFFFFF);
            guiGraphics.centeredText(this.font, "for " + this.generator.maxBurnTime + " t", this.leftPos + 87, this.topPos + 75, 0xFFFFFFFF);
            Matrix3x2fStack poseStack = guiGraphics.pose();
            poseStack.pushMatrix();
            poseStack.translate(this.leftPos + 87, this.topPos + 85);
            poseStack.scale(0.5625F, 0.5625F);
            int usage = this.generator.fuelUsage;
            guiGraphics.centeredText(this.font, "(per " + usage + " mB)",0, 0, 0xFFFFFFFF);
            poseStack.popMatrix();
        }

        this.energy.draw(guiGraphics);
        this.fluid.draw(guiGraphics);
    }
}
