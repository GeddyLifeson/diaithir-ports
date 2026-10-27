package com.github.alexthe666.citadel.client.render.legacy;

import com.github.alexthe666.citadel.client.model.basic.BasicEntityModel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;

/** 1.21 EnergySwirlLayer (charged-creeper glow) for Citadel models. */
public abstract class LegacyEnergySwirlLayer<T extends LivingEntity, M extends BasicEntityModel<T>> extends LegacyLayer<T, M> {
    public LegacyEnergySwirlLayer(LegacyLayerParent<T, M> renderer) {
        super(renderer);
    }

    protected abstract boolean isPowered(T entity);

    protected abstract float xOffset(float tick);

    protected abstract Identifier getTextureLocation();

    protected abstract M model();

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (!isPowered(entity)) return;
        float tick = (float) entity.tickCount + partialTicks;
        M model = this.model();
        model.prepareMobModel(entity, limbSwing, limbSwingAmount, partialTicks);
        this.getParentModel().copyPropertiesTo(model);
        model.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        var vc = buffer.getBuffer(RenderTypes.energySwirl(getTextureLocation(), xOffset(tick) % 1.0F, tick * 0.01F % 1.0F));
        model.renderToBuffer(poseStack, vc, packedLight, OverlayTexture.NO_OVERLAY, -8355712);
    }
}
