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

import com.buuz135.industrial.block.generator.tile.MycelialReactorTile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.awt.*;

public class MycelialReactorTESR implements BlockEntityRenderer<MycelialReactorTile, MycelialReactorTESR.State> {

    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath("industrialforegoing", "textures/block/mycelial.png");

    public MycelialReactorTESR(BlockEntityRendererProvider.Context p_173540_) {

    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(MycelialReactorTile te, State state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(te, state, partialTicks, cameraPosition, breakProgress);
        state.active = te.getBar().getProgress() != 0;
        state.gameTime = te.getLevel().getGameTime();
    }

    @Override
    public void submit(State state, PoseStack stack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (!state.active) return;
        stack.pushPose();
        stack.scale(1.5F, 1.5F, 1.5F);
        stack.translate(0.30, 1.75, 0.3D);
        stack.mulPose(camera.orientation);
        stack.mulPose(Axis.ZP.rotationDegrees((state.gameTime / 20F) % 360));
        stack.mulPose(Axis.XP.rotationDegrees(-90f));

        double speed = 20;
        collector.submitCustomGeometry(stack, RenderTypes.entityTranslucent(TEXTURE), (pose, buffer1) -> {
            float sin = ((float) Math.sin(state.gameTime / speed)) * 0.05f;
            float pX1 = 1;
            float u = 1;
            float pX2 = 0;
            float u2 = 0;
            int color = Color.CYAN.getRGB();
            int red = ARGB.red(color);
            int green = ARGB.green(color);
            int blue = ARGB.blue(color);
            float xOffset = -0.75f;
            float yOffset = -0f;
            float zOffset = -0.75f;
            int alpha = 255;
            buffer1.addVertex(pose, pX2 + xOffset + sin, yOffset, 0 + zOffset + sin).setUv(u2, 0).setColor(red, green, blue, alpha).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightCoordsUtil.FULL_BRIGHT).setNormal(pose, 0, 1, 0);
            buffer1.addVertex(pose, pX1 + xOffset - sin + 0.5f, yOffset, 0 + zOffset + sin).setUv(u, 0).setColor(red, green, blue, alpha).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightCoordsUtil.FULL_BRIGHT).setNormal(pose, 0, 1, 0);
            buffer1.addVertex(pose, pX1 + xOffset - sin + 0.5f, yOffset, 1.5f + zOffset - sin).setUv(u, 1).setColor(red, green, blue, alpha).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightCoordsUtil.FULL_BRIGHT).setNormal(pose, 0, 1, 0);
            buffer1.addVertex(pose, pX2 + xOffset + sin, yOffset, 1.5f + zOffset - sin).setUv(u2, 1).setColor(red, green, blue, alpha).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightCoordsUtil.FULL_BRIGHT).setNormal(pose, 0, 1, 0);
            yOffset = 0.01f;
            sin = ((float) Math.cos(state.gameTime / speed)) * 0.05f;
            color = new Color(0xB578FF).getRGB();
            red = ARGB.red(color);
            green = ARGB.green(color);
            blue = ARGB.blue(color);
            buffer1.addVertex(pose, pX2 + xOffset + sin, yOffset, 0 + zOffset + sin).setUv(u2, 0).setColor(red, green, blue, alpha).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightCoordsUtil.FULL_BRIGHT).setNormal(pose, 0, 1, 0);
            buffer1.addVertex(pose, pX1 + xOffset - sin + 0.5f, yOffset, 0 + zOffset - sin).setUv(u, 0).setColor(red, green, blue, alpha).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightCoordsUtil.FULL_BRIGHT).setNormal(pose, 0, 1, 0);
            buffer1.addVertex(pose, pX1 + xOffset + sin + 0.5f, yOffset, 1.5f + zOffset + sin).setUv(u, 1).setColor(red, green, blue, alpha).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightCoordsUtil.FULL_BRIGHT).setNormal(pose, 0, 1, 0);
            buffer1.addVertex(pose, pX2 + xOffset - sin, yOffset, 1.5f + zOffset + sin).setUv(u2, 1).setColor(red, green, blue, alpha).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightCoordsUtil.FULL_BRIGHT).setNormal(pose, 0, 1, 0);
        });
        stack.popPose();
    }

    @Override
    public AABB getRenderBoundingBox(MycelialReactorTile blockEntity) {
        return BlockEntityRenderer.super.getRenderBoundingBox(blockEntity).inflate(4);
    }

    public static class State extends BlockEntityRenderState {
        boolean active;
        long gameTime;
    }
}
