package com.github.alexmodguy.alexscaves.client.render.entity.layer;

import com.github.alexthe666.citadel.client.render.legacy.LegacyMobRenderer;
import com.github.alexmodguy.alexscaves.client.ClientProxy;
import com.github.alexmodguy.alexscaves.client.render.ACRenderTypes;
import com.github.alexmodguy.alexscaves.client.render.ColorUtil;
import com.github.alexmodguy.alexscaves.server.entity.living.LicowitchEntity;
import com.github.alexmodguy.alexscaves.server.entity.util.PossessedByLicowitch;
import com.github.alexthe666.citadel.client.model.AdvancedEntityModel;
import com.github.alexthe666.citadel.client.shader.PostEffectRegistry;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import com.github.alexthe666.citadel.client.render.legacy.LegacyLayerParent;
import com.github.alexthe666.citadel.client.render.legacy.LegacyLayer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;

import java.util.function.Function;

public class LicowitchPossessionLayer<T extends LivingEntity & PossessedByLicowitch, M extends AdvancedEntityModel<T>> extends LegacyLayer<T, M> {

    private AdvancedEntityModel<T> replacementModel;
    private Function<T, Identifier> replacementTexture;

    public LicowitchPossessionLayer(LegacyLayerParent<T, M> renderLayerParent) {
        super(renderLayerParent);
    }

    public LicowitchPossessionLayer(LegacyLayerParent<T, M> renderLayerParent, Function<T, Identifier> replacementTexture) {
        super(renderLayerParent);
        this.replacementTexture = replacementTexture;
    }

    public LicowitchPossessionLayer(LegacyLayerParent<T, M> renderLayerParent, AdvancedEntityModel<T> replacementModel, Function<T, Identifier> replacementTexture) {
        super(renderLayerParent);
        this.replacementModel = replacementModel;
        this.replacementTexture = replacementTexture;
    }

    public void render(PoseStack poseStack, MultiBufferSource bufferIn, int packedLightIn, T entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (entity.getPossessedByLicowitchId() != -1) {
            LicowitchEntity licowitch = entity.getPossessingLicowitch(entity.level());
            if (licowitch != null) {
                AdvancedEntityModel<T> model = getParentModel();
                Identifier texture = getTextureLocation(entity);
                if (replacementTexture != null) {
                    texture = replacementTexture.apply(entity);
                }
                if (replacementModel != null) {
                    model = replacementModel;
                    this.getParentModel().copyPropertiesTo(model);
                    model.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
                }
                float possessAlpha = (float) (0.35F + (1F + Math.sin(ageInTicks * 0.2F)) * 0.15F);
                PostEffectRegistry.renderEffectForNextTick(ClientProxy.PURPLE_WITCH_SHADER);
                model.renderToBuffer(poseStack, bufferIn.getBuffer(RenderTypes.entityTranslucent(texture)), packedLightIn, com.github.alexthe666.citadel.client.render.legacy.LegacyMobRenderer.getOverlayCoords(entity, 0.0F), ColorUtil.packColor(possessAlpha, 1.0F, 0.3F, 1.0F));
                model.renderToBuffer(poseStack, bufferIn.getBuffer(ACRenderTypes.getPurpleWitch(texture)), packedLightIn, com.github.alexthe666.citadel.client.render.legacy.LegacyMobRenderer.getOverlayCoords(entity, 0), ColorUtil.packColor(1.0F, 1F, 1F, 1F));
            }
        }
    }
}
