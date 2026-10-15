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

package com.buuz135.industrial.entity.client;

import com.buuz135.industrial.entity.InfinityNukeEntity;
import com.buuz135.industrial.utils.Reference;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Unit;

public class InfinityNukeRenderer extends EntityRenderer<InfinityNukeEntity, InfinityNukeRenderer.NukeRenderState> {

    public static final Identifier NUKE = Identifier.fromNamespaceAndPath(Reference.MOD_ID, "textures/entity/infinity_nuke_entity.png");
    private static final Identifier SWIRL = Identifier.fromNamespaceAndPath(Reference.MOD_ID, "textures/block/mycelial_clean.png");

    private final InfinityNukeModel nukeModel;
    private final InfinityNukeModelArmed nukeModelArmed;
    private final InfinityNukeModelArmed nukeModelArmedBig;

    public static final ModelLayerLocation NUKE_LAYER = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Reference.MOD_ID, "infinity_nuke_entity"), "main");
    public static final ModelLayerLocation NUKE_ARMED_LAYER = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Reference.MOD_ID, "infinity_nuke_entity"), "armed");
    public static final ModelLayerLocation NUKE_ARMED_BIG_LAYER = new ModelLayerLocation(Identifier.fromNamespaceAndPath(Reference.MOD_ID, "infinity_nuke_entity"), "armed_big");

    public InfinityNukeRenderer(EntityRendererProvider.Context p_174008_) {
        super(p_174008_);
        nukeModel = new InfinityNukeModel(p_174008_.bakeLayer(NUKE_LAYER));
        nukeModelArmed = new InfinityNukeModelArmed(p_174008_.bakeLayer(NUKE_ARMED_LAYER));
        nukeModelArmedBig = new InfinityNukeModelArmed(p_174008_.bakeLayer(NUKE_ARMED_BIG_LAYER));
    }

    @Override
    public NukeRenderState createRenderState() {
        return new NukeRenderState();
    }

    @Override
    public void extractRenderState(InfinityNukeEntity entity, NukeRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.xRot = entity.getXRot(partialTicks);
        state.armed = entity.isDataArmed();
        state.exploding = entity.isDataExploding();
        state.ticksExploding = entity.getDataTicksExploding();
        double shake = 7 + entity.level().getRandom().nextInt(50);
        state.shakeX = (entity.level().getRandom().nextDouble() - 0.5) / shake;
        state.shakeZ = (entity.level().getRandom().nextDouble() - 0.5) / shake;
        state.showSwirl = entity.level().getRandom().nextDouble() < 0.96;
    }

    @Override
    public void submit(NukeRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.ZP.rotationDegrees(state.xRot - 180.0F));
        poseStack.translate(0, -1.35, 0.05);
        if (state.armed) {
            if (state.exploding) {
                poseStack.translate(state.shakeX, 0, state.shakeZ);
            }
            collector.submitModel(nukeModelArmed, Unit.INSTANCE, poseStack, NUKE, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor, null);
            if (state.exploding && state.showSwirl) {
                float f = state.partialTick + state.ticksExploding + 10;
                float offset = f * (state.ticksExploding / 50000f);
                collector.submitModel(nukeModelArmedBig, Unit.INSTANCE, poseStack, RenderTypes.energySwirl(SWIRL, offset, offset), state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor, null);
            }
        } else {
            collector.submitModel(nukeModel, Unit.INSTANCE, poseStack, NUKE, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor, null);
        }
        poseStack.popPose();
        super.submit(state, poseStack, collector, camera);
    }

    public static class NukeRenderState extends EntityRenderState {
        public float xRot;
        public boolean armed;
        public boolean exploding;
        public int ticksExploding;
        public double shakeX;
        public double shakeZ;
        public boolean showSwirl;
    }
}
