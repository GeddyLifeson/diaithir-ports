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

package com.buuz135.industrial.proxy.client.event;

import com.buuz135.industrial.item.infinity.InfinityTier;
import com.buuz135.industrial.item.infinity.item.ItemInfinityDrill;
import com.buuz135.industrial.module.ModuleTool;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ExtractBlockOutlineRenderStateEvent;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

public class IFClientEvents {

    @SubscribeEvent
    public void blockOverlayEvent(ExtractBlockOutlineRenderStateEvent event) {
        ItemStack hand = Minecraft.getInstance().player.getMainHandItem();
        if (!hand.getItem().equals(ModuleTool.INFINITY_DRILL.get())) return;
        BlockHitResult blockRayTraceResult = event.getHitResult();
        InfinityTier tier = ((ItemInfinityDrill) ModuleTool.INFINITY_DRILL.get()).getSelectedTier(hand);
        Level world = event.getLevel();
        Pair<BlockPos, BlockPos> area = ((ItemInfinityDrill) ModuleTool.INFINITY_DRILL.get()).getArea(blockRayTraceResult.getBlockPos(), blockRayTraceResult.getDirection(), tier, false);
        List<Pair<BlockPos, VoxelShape>> shapes = new ArrayList<>();
        BlockPos.betweenClosed(area.getLeft(), area.getRight()).forEach(blockPos -> {
            VoxelShape shape = world.getBlockState(blockPos).getShape(world, blockPos);
            if (shape != null && !shape.isEmpty() && !world.isEmptyBlock(blockPos) && world.getBlockState(blockPos).getDestroySpeed(world, blockPos) >= 0 && !(world.getBlockState(blockPos).getBlock() instanceof LiquidBlock)) {
                shapes.add(Pair.of(blockPos.immutable(), shape));
            }
        });
        Vec3 camera = event.getCamera().position();
        event.addCustomRenderer((state, bufferSource, poseStack, translucentPass, levelRenderState) -> {
            if (!translucentPass) {
                VertexConsumer builder = bufferSource.getBuffer(RenderTypes.lines());
                shapes.forEach(entry -> ShapeRenderer.renderShape(poseStack, builder, Shapes.create(entry.getRight().bounds()), entry.getLeft().getX() - camera.x, entry.getLeft().getY() - camera.y, entry.getLeft().getZ() - camera.z, ARGB.color(0.35F, 0), 2.5F));
            }
            return true;
        });
    }
}
