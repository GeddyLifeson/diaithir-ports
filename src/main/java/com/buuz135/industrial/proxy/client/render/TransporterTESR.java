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

import com.buuz135.industrial.api.transporter.TransporterType;
import com.buuz135.industrial.api.transporter.TransporterTypeFactory;
import com.buuz135.industrial.block.transportstorage.tile.TransporterTile;
import com.buuz135.industrial.block.transportstorage.transporter.TransporterItemType;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransporterTESR implements BlockEntityRenderer<TransporterTile, TransporterTESR.State> {

    public static Identifier TEXTURE = Identifier.fromNamespaceAndPath("industrialforegoing", "textures/block/transporters/particle.png");

    // additive, emissive textured quads
    public static RenderType TYPE = RenderTypes.energySwirl(TEXTURE, 0, 0);

    public TransporterTESR(BlockEntityRendererProvider.Context context) {
    }

    /**
     * Draws the camera-facing glow quad that stands in for an empty transfer; the quad keeps the 1.5 x 1.5 shape of the original.
     */
    public static void submitParticle(PoseStack stack, SubmitNodeCollector collector, CameraRenderState camera, float scale, float red, float green, float blue) {
        stack.pushPose();
        stack.mulPose(camera.orientation);
        stack.mulPose(Axis.XP.rotationDegrees(-90f));
        stack.scale(scale, scale, scale);
        float pX1 = 1;
        float u = 1;
        float pX2 = 0;
        float u2 = 0;
        float xOffset = -0.75f;
        float yOffset = -0f;
        float zOffset = -0.75f;
        collector.submitCustomGeometry(stack, TYPE, (pose, buffer) -> {
            buffer.addVertex(pose, pX2 + xOffset, yOffset, 0 + zOffset).setColor(red, green, blue, 1f).setUv(u2, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightCoordsUtil.FULL_BRIGHT).setNormal(pose, 0, 1, 0);
            buffer.addVertex(pose, pX1 + xOffset + 0.5f, yOffset, 0 + zOffset).setColor(red, green, blue, 1f).setUv(u, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightCoordsUtil.FULL_BRIGHT).setNormal(pose, 0, 1, 0);
            buffer.addVertex(pose, pX1 + xOffset + 0.5f, yOffset, 1.5f + zOffset).setColor(red, green, blue, 1f).setUv(u, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightCoordsUtil.FULL_BRIGHT).setNormal(pose, 0, 1, 0);
            buffer.addVertex(pose, pX2 + xOffset, yOffset, 1.5f + zOffset).setColor(red, green, blue, 1f).setUv(u2, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightCoordsUtil.FULL_BRIGHT).setNormal(pose, 0, 1, 0);
        });
        stack.popPose();
    }

    public static Vector3f getPath(Direction from, Direction to, double step, float partialTicks) {
        float totalSteps = 5f;
        if (from.getOpposite() == to) {
            totalSteps = 7f;
            Vec3 vector3d = new Vec3(to.step().x() / totalSteps * step, to.step().y() / totalSteps * step, to.step().z() / totalSteps * step);
            if (from.getAxis() == Direction.Axis.X) {
                vector3d = vector3d.add(0, 0.5, 0.5);
                if (from.getAxisDirection() == Direction.AxisDirection.POSITIVE) {
                    vector3d = vector3d.add(1, 0, 0);
                }
            }
            if (from.getAxis() == Direction.Axis.Z) {
                vector3d = vector3d.add(0.5, 0.5, 0);
                if (from.getAxisDirection() == Direction.AxisDirection.POSITIVE) {
                    vector3d = vector3d.add(0, 0, 1);
                }
            }
            if (from.getAxis() == Direction.Axis.Y) {
                vector3d = vector3d.add(0.5, 0, 0.5);
                if (from.getAxisDirection() == Direction.AxisDirection.POSITIVE) {
                    vector3d = vector3d.add(0, 1, 0);
                }
            }
            float divideSecond = -0.22f;
            vector3d = vector3d.add(from.step().x() * divideSecond, from.step().y() * divideSecond, from.step().z() * divideSecond);
            return new Vector3f((float) vector3d.x, (float) vector3d.y, (float) vector3d.z);
        }
        Vector3f vsrc = from.step();
        Vector3f vdst = to.step();
        Vector3f a = new Vector3f(vsrc);
        a.mul(5 / 16f);
        Vector3f b = new Vector3f(vdst);
        b.mul(6 / 16f);
        Vector3f c = new Vector3f(vsrc);
        c.mul(3 / 16f);
        float sind = -Mth.sin((float) ((step / totalSteps) * Math.PI / 2f));
        float cosd = -Mth.cos((float) ((step / totalSteps) * Math.PI / 2f));
        a.mul(sind);
        b.mul(cosd);
        a.add(b);
        a.add(c);
        float divide = 2.5f;
        a.add(vdst.x() / divide, vdst.y() / divide, vdst.z() / divide);
        a.add(0.5f, 0.5f, 0.5f);
        float divideSecond = 0.15f;
        a.add(vsrc.x() * divideSecond, vsrc.y() * divideSecond, vsrc.z() * divideSecond);
        return a;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(TransporterTile tile, State state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(tile, state, partialTicks, cameraPosition, breakProgress);
        state.transporters = new HashMap<>(tile.getTransporterTypeMap());
        state.level = tile.getLevel();
        state.partialTicks = partialTicks;
        state.upgradeModels.clear();
        for (TransporterType type : state.transporters.values()) {
            if (type != null) state.upgradeModels.add(type.getFactory().getModel(type.getSide(), type.getAction()));
        }
    }

    @Override
    public void submit(State state, PoseStack stack, SubmitNodeCollector collector, CameraRenderState camera) {
        for (Identifier model : state.upgradeModels) {
            UpgradeModels.submit(model, stack, collector, state.lightCoords);
        }
        Map<Direction, TransporterType> transporters = state.transporters;
        for (Direction direction : transporters.keySet()) {
            if (transporters.get(direction).getAction() == TransporterTypeFactory.TransporterAction.EXTRACT) {
                for (Direction other : transporters.keySet()) {
                    if (direction == other || !transporters.get(direction).getFactory().getName().equals(transporters.get(other).getFactory().getName()) || transporters.get(other).getAction() == TransporterTypeFactory.TransporterAction.EXTRACT)
                        continue;
                    for (int i = -1; i < TransporterItemType.QUEUE_SIZE; ++i) {
                        stack.pushPose();
                        Vector3f pos = getPath(direction, other, i + state.partialTicks, state.partialTicks);
                        stack.translate(pos.x(), pos.y(), pos.z());
                        transporters.get(other).renderTransfer(pos, direction, i + 1, stack, collector, camera, state.partialTicks, state.level);
                        stack.popPose();
                    }
                }
            }
        }
    }

    public static class State extends BlockEntityRenderState {
        final List<Identifier> upgradeModels = new ArrayList<>();
        Map<Direction, TransporterType> transporters = Map.of();
        Level level;
        float partialTicks;
    }
}
