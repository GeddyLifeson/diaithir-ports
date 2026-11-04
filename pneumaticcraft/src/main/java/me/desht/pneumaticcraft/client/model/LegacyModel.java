package me.desht.pneumaticcraft.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

/** Minimal stand-in for the pre-26.1 EntityModel, for the ported hand-written PneumaticCraft models. */
public abstract class LegacyModel {
    public abstract void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int light, int overlay, int color);
}
