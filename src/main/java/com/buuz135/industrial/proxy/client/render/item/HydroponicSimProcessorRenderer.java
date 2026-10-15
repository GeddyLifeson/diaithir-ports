/*
 * This file is part of Industrial Foregoing.
 *
 * Copyright 2021, Buuz135
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in the
 * Software without restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to the
 * following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies
 * or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.buuz135.industrial.proxy.client.render.item;

import com.buuz135.industrial.item.HydroponicSimulationProcessorItem;
import com.buuz135.industrial.utils.IFAttachments;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;
import org.joml.Vector3fc;

import java.util.function.Consumer;

/**
 * Draws the simulated crop on a corner of the processor; the processor model itself comes from the item definition.
 */
public class HydroponicSimProcessorRenderer implements SpecialModelRenderer<ItemStack> {

    @Override
    public void submit(@Nullable ItemStack crop, PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
        if (crop == null) return;
        ItemStackRenderState renderState = new ItemStackRenderState();
        Minecraft.getInstance().getItemModelResolver().updateForTopItem(renderState, crop, ItemDisplayContext.GUI, Minecraft.getInstance().level, null, 0);
        poseStack.pushPose();
        poseStack.translate(0.1, -0.1, 0.001);
        poseStack.scale(0.5f, 0.5f, 1);
        renderState.submit(poseStack, collector, lightCoords, overlayCoords, outlineColor);
        poseStack.popPose();
    }

    @Override
    public void getExtents(Consumer<Vector3fc> output) {
    }

    @Override
    public @Nullable ItemStack extractArgument(ItemStack stack) {
        if (!stack.has(IFAttachments.HYDROPONIC_SIMULATION_PROCESSOR)) return null;
        ItemStack crop = new HydroponicSimulationProcessorItem.Simulation(stack.get(IFAttachments.HYDROPONIC_SIMULATION_PROCESSOR)).getCrop();
        return crop.isEmpty() ? null : crop;
    }

    public record Unbaked() implements SpecialModelRenderer.Unbaked<ItemStack> {

        public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(new Unbaked());

        @Override
        public SpecialModelRenderer<ItemStack> bake(BakingContext context) {
            return new HydroponicSimProcessorRenderer();
        }

        @Override
        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }
    }
}
