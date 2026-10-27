package com.github.alexthe666.citadel.client.render.legacy;

import com.github.alexthe666.citadel.client.model.basic.BasicEntityModel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;

/** 1.21-style render layer (immediate mode). Drawn through the recording buffer source. */
public abstract class LegacyLayer<T extends LivingEntity, M extends BasicEntityModel<T>> {
    private final LegacyLayerParent<T, M> renderer;

    public LegacyLayer(LegacyLayerParent<T, M> renderer) {
        this.renderer = renderer;
    }

    public M getParentModel() {
        return renderer.getModel();
    }

    public LegacyLayerParent<T, M> getRenderer() {
        return renderer;
    }

    public abstract void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch);
}
