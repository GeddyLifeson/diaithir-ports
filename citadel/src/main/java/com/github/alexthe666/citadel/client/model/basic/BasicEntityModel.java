package com.github.alexthe666.citadel.client.model.basic;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.world.entity.Entity;

/**
 * Citadel's entity model base. In 1.21 this extended the vanilla EntityModel; in 26.1 vanilla models are ModelPart/render-state
 * based, so this is a plain class with the old entity-based animation hooks. It is drawn by {@code CitadelRenderer}.
 */
public abstract class BasicEntityModel<T extends Entity> {
    public int textureWidth = 64;
    public int textureHeight = 32;
    /** Set while rendering a baby, matches the old vanilla field some models read. */
    public boolean young;

    protected BasicEntityModel() {
    }

    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLightIn, int packedOverlayIn, int color) {
        this.parts().forEach((part) -> part.render(poseStack, vertexConsumer, packedLightIn, packedOverlayIn, color));
    }

    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLightIn, int packedOverlayIn) {
        this.renderToBuffer(poseStack, vertexConsumer, packedLightIn, packedOverlayIn, -1);
    }

    public abstract Iterable<BasicModelPart> parts();

    public abstract void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch);

    public void copyPropertiesTo(BasicEntityModel<T> other) {
        other.young = this.young;
    }

    public void prepareMobModel(T entity, float limbSwing, float limbSwingAmount, float partialTick) {
    }
}
