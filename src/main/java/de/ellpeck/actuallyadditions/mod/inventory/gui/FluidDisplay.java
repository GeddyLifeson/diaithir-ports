/*
 * This file ("FluidDisplay.java") is part of the Actually Additions mod for Minecraft.
 * It is created and owned by Ellpeck and distributed
 * under the Actually Additions License to be found at
 * http://ellpeck.de/actaddlicense
 * View the source code at https://github.com/Ellpeck/ActuallyAdditions
 *
 * © 2015-2017 Ellpeck
 */

package de.ellpeck.actuallyadditions.mod.inventory.gui;

import net.minecraft.client.renderer.block.FluidModel;

import net.minecraft.client.renderer.RenderPipelines;

import de.ellpeck.actuallyadditions.mod.util.AssetUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import javax.annotation.Nonnull;
import java.text.NumberFormat;
import java.util.Collections;


public class FluidDisplay {

    private IFluidTank fluidReference;

    private int x;
    private int y;
    private boolean outline;


    private boolean drawTextNextTo;

    private boolean drawCapacityInTooltip = true;

    public FluidDisplay(int x, int y, IFluidTank fluidReference, boolean outline, boolean drawTextNextTo) {
        this.setData(x, y, fluidReference, outline, drawTextNextTo);
    }

    public FluidDisplay(int x, int y, IFluidTank fluidReference) {
        this(x, y, fluidReference, false, false);
    }

    public FluidDisplay(int x, int y, FluidStack stack, int capacity, boolean drawCapacity) {
        this.x = x;
        this.y = y;
        this.drawCapacityInTooltip = drawCapacity;
        this.fluidReference = new DummyTank(stack, capacity);
    }

    public void setDrawCapacityInTooltip(boolean drawCapacityInTooltip) {
        this.drawCapacityInTooltip = drawCapacityInTooltip;
    }

    public void setData(int x, int y, IFluidTank fluidReference, boolean outline, boolean drawTextNextTo) {
        this.x = x;
        this.y = y;
        this.fluidReference = fluidReference;
        this.outline = outline;
        this.drawTextNextTo = drawTextNextTo;
    }

    public void draw(GuiGraphicsExtractor guiGraphics) {
        Minecraft mc = Minecraft.getInstance();

        int barX = this.x;
        int barY = this.y;

        if (this.outline) {
	        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, AssetUtil.GUI_INVENTORY_LOCATION, this.x, this.y, 52, 163, 26, 93, 256, 256);

            barX += 4;
            barY += 4;
        }
	    guiGraphics.blit(RenderPipelines.GUI_TEXTURED, AssetUtil.GUI_INVENTORY_LOCATION, barX, barY, 0, 171, 18, 85, 256, 256);

        FluidStack stack = this.fluidReference.getFluid();

        if (!stack.isEmpty()) {
            // 26.1: a fluid's sprite and tint live in its FluidModel
            FluidModel model = Minecraft.getInstance().getModelManager().getFluidStateModelSet().get(stack.getFluid().defaultFluidState());
            TextureAtlasSprite sprite = model.stillMaterial().sprite();
            int color = model.fluidTintSource() != null ? model.fluidTintSource().colorAsStack(stack) : -1;
            int level = (int) Math.ceil((float) this.fluidReference.getFluidAmount() / this.fluidReference.getCapacity() * 83);

            // stack 16px tiles up from the bottom of the bar, clipped to the fill level
            int bottom = barY + 84;
            guiGraphics.enableScissor(barX + 1, bottom - level, barX + 17, bottom);
            for (int top = bottom - 16; top > bottom - level - 16; top -= 16) {
                guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, barX + 1, top, 16, 16, color);
            }
            guiGraphics.disableScissor();

            if (this.drawTextNextTo) {
                guiGraphics.text(mc.font, this.getOverlayText(), barX + 25, barY + 78, 0xFFFFFFFF, false);
            }
        }
    }


    public void render(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        if (mouseX >= this.x && mouseY >= this.y && mouseX < this.x + (this.outline
            ? 26
            : 18) && mouseY < this.y + (this.outline
            ? 93
            : 85)) {
            Minecraft mc = Minecraft.getInstance();
            guiGraphics.setComponentTooltipForNextFrame(mc.font, Collections.singletonList(this.getOverlayText()), mouseX, mouseY);
        }
    }

    private Component getOverlayText() {
        NumberFormat format = NumberFormat.getInstance();
        FluidStack stack = this.fluidReference.getFluid();
        String cap = format.format(this.fluidReference.getCapacity());
        return stack.isEmpty()
            ? Component.literal("0/" + cap + " mB")
            : Component.literal(format.format(this.fluidReference.getFluidAmount()) + (drawCapacityInTooltip?"/" + cap + " mB ":" mB ")).append(stack.getHoverName());
    }

    public static class DummyTank implements IFluidTank {
        private final FluidStack fluid;
        private final int capacity;

        public DummyTank(FluidStack fluid, int capacity) {
            this.fluid = fluid;
            this.capacity = capacity;
        }

        @Nonnull
        @Override
        public FluidStack getFluid() {
            return fluid;
        }

        @Override
        public int getFluidAmount() {
            return fluid.getAmount();
        }

        @Override
        public int getCapacity() {
            return capacity;
        }

        @Override
        public boolean isFluidValid(FluidStack stack) {
            return false;
        }

        @Override
        public int fill(FluidStack resource, IFluidHandler.FluidAction action) {
            return 0;
        }

        @Nonnull
        @Override
        public FluidStack drain(int maxDrain, IFluidHandler.FluidAction action) {
            return FluidStack.EMPTY;
        }

        @Nonnull
        @Override
        public FluidStack drain(FluidStack resource, IFluidHandler.FluidAction action) {
            return FluidStack.EMPTY;
        }
    }
}
