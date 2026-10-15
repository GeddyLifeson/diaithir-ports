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

package com.buuz135.industrial.proxy.client.render;

import com.buuz135.industrial.block.tile.IndustrialAreaWorkingTile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import org.jspecify.annotations.Nullable;
import org.joml.Vector3f;

import java.awt.*;

public class WorkingAreaTESR implements BlockEntityRenderer<IndustrialAreaWorkingTile, WorkingAreaTESR.State> {

    public WorkingAreaTESR(BlockEntityRendererProvider.Context p_173540_) {

    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(IndustrialAreaWorkingTile tile, State state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(tile, state, partialTicks, cameraPosition, breakProgress);
        state.showing = tile.isShowingArea();
        if (!state.showing) return;
        BlockPos pos = tile.getBlockPos();
        state.area = tile.getWorkingArea().bounds().inflate(0.001).move(-pos.getX(), -pos.getY(), -pos.getZ());
        Color color = new Color(Math.abs(pos.getX() % 255), Math.abs(pos.getY() % 255), Math.abs(pos.getZ() % 255));
        state.color = color.getRGB();
        state.lineWidth = Math.max(2.5F, (float) Minecraft.getInstance().getWindow().getWidth() / 1920.0F * 2.5F);
    }

    @Override
    public void submit(State state, PoseStack stack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (!state.showing) return;
        collector.submitCustomGeometry(stack, RenderTypes.lines(), (pose, buffer) -> renderLines(pose, buffer, state.area, ARGB.color(0.5F, state.color), state.lineWidth));
        collector.submitCustomGeometry(stack, RenderTypes.debugQuads(), (pose, buffer) -> renderFaces(pose, buffer, state.area, ARGB.redFloat(state.color), ARGB.greenFloat(state.color), ARGB.blueFloat(state.color), 0.3F));
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    private void renderLines(PoseStack.Pose pose, VertexConsumer buffer, AABB box, int color, float width) {
        Shapes.create(box).forAllEdges((x1, y1, z1, x2, y2, z2) -> {
            Vector3f normal = new Vector3f((float) (x2 - x1), (float) (y2 - y1), (float) (z2 - z1)).normalize();
            buffer.addVertex(pose, (float) x1, (float) y1, (float) z1).setColor(color).setNormal(pose, normal).setLineWidth(width);
            buffer.addVertex(pose, (float) x2, (float) y2, (float) z2).setColor(color).setNormal(pose, normal).setLineWidth(width);
        });
    }

    private void renderFaces(PoseStack.Pose pose, VertexConsumer buffer, AABB pos, float red, float green, float blue, float alpha) {

        float x1 = (float) pos.minX;
        float x2 = (float) pos.maxX;
        float y1 = (float) pos.minY;
        float y2 = (float) pos.maxY;
        float z1 = (float) pos.minZ;
        float z2 = (float) pos.maxZ;

        buffer.addVertex(pose, x1, y1, z1).setColor(red, green, blue, alpha);
        buffer.addVertex(pose, x1, y2, z1).setColor(red, green, blue, alpha);
        buffer.addVertex(pose, x2, y2, z1).setColor(red, green, blue, alpha);
        buffer.addVertex(pose, x2, y1, z1).setColor(red, green, blue, alpha);

        buffer.addVertex(pose, x1, y1, z2).setColor(red, green, blue, alpha);
        buffer.addVertex(pose, x2, y1, z2).setColor(red, green, blue, alpha);
        buffer.addVertex(pose, x2, y2, z2).setColor(red, green, blue, alpha);
        buffer.addVertex(pose, x1, y2, z2).setColor(red, green, blue, alpha);


        buffer.addVertex(pose, x1, y1, z1).setColor(red, green, blue, alpha);
        buffer.addVertex(pose, x2, y1, z1).setColor(red, green, blue, alpha);
        buffer.addVertex(pose, x2, y1, z2).setColor(red, green, blue, alpha);
        buffer.addVertex(pose, x1, y1, z2).setColor(red, green, blue, alpha);

        buffer.addVertex(pose, x1, y2, z1).setColor(red, green, blue, alpha);
        buffer.addVertex(pose, x1, y2, z2).setColor(red, green, blue, alpha);
        buffer.addVertex(pose, x2, y2, z2).setColor(red, green, blue, alpha);
        buffer.addVertex(pose, x2, y2, z1).setColor(red, green, blue, alpha);


        buffer.addVertex(pose, x1, y1, z1).setColor(red, green, blue, alpha);
        buffer.addVertex(pose, x1, y1, z2).setColor(red, green, blue, alpha);
        buffer.addVertex(pose, x1, y2, z2).setColor(red, green, blue, alpha);
        buffer.addVertex(pose, x1, y2, z1).setColor(red, green, blue, alpha);

        buffer.addVertex(pose, x2, y1, z1).setColor(red, green, blue, alpha);
        buffer.addVertex(pose, x2, y2, z1).setColor(red, green, blue, alpha);
        buffer.addVertex(pose, x2, y2, z2).setColor(red, green, blue, alpha);
        buffer.addVertex(pose, x2, y1, z2).setColor(red, green, blue, alpha);

    }

    @Override
    public AABB getRenderBoundingBox(IndustrialAreaWorkingTile blockEntity) {
        return blockEntity.getWorkingArea().bounds();
    }

    public static class State extends BlockEntityRenderState {
        boolean showing;
        AABB area;
        int color;
        float lineWidth;
    }
}
