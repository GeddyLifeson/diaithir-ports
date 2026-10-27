package com.github.alexmodguy.alexscaves.client.render.entity;

import com.github.alexmodguy.alexscaves.client.render.ColorUtil;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.github.alexmodguy.alexscaves.client.model.DeepOneKnightModel;
import com.github.alexmodguy.alexscaves.server.entity.living.DeepOneKnightEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import com.github.alexthe666.citadel.client.render.legacy.LegacyMobRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import com.github.alexthe666.citadel.client.render.legacy.LegacyLayer;
import net.minecraft.resources.Identifier;

public class DeepOneKnightRenderer extends LegacyMobRenderer<DeepOneKnightEntity, DeepOneKnightModel> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/deep_one/deep_one_knight.png");
    private static final Identifier TEXTURE_NOON = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/deep_one/deep_one_knight_noon.png");
    private static final Identifier TEXTURE_GLOW = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/deep_one/deep_one_knight_glow.png");

    public DeepOneKnightRenderer(EntityRendererProvider.Context renderManagerIn) {
        super(renderManagerIn, new DeepOneKnightModel(), 0.45F);
        this.addLayer(new LayerGlow());
        this.addLayer(new com.github.alexthe666.citadel.client.render.legacy.LegacyItemInHandLayer<>(this));
    }

    @Override
    protected void scale(DeepOneKnightEntity mob, PoseStack matrixStackIn, float partialTicks) {
        if (mob.isSummoned()) {
            matrixStackIn.translate(0, (mob.getBbHeight() + 1F) * (1F - mob.getSummonProgress(partialTicks)), 0);
        }
    }

    public Identifier getTextureLocation(DeepOneKnightEntity entity) {
        return entity.isNoon() ? TEXTURE_NOON : TEXTURE;
    }

    class LayerGlow extends LegacyLayer<DeepOneKnightEntity, DeepOneKnightModel> {

        public LayerGlow() {
            super(DeepOneKnightRenderer.this);
        }

        public void render(PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, DeepOneKnightEntity entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
            if (!entitylivingbaseIn.isInvisible() && !entitylivingbaseIn.isNoon()) {
                VertexConsumer ivertexbuilder = bufferIn.getBuffer(RenderTypes.eyes(TEXTURE_GLOW));
                float alpha = 1.0F;
                this.getParentModel().renderToBuffer(matrixStackIn, ivertexbuilder, 240, com.github.alexthe666.citadel.client.render.legacy.LegacyMobRenderer.getOverlayCoords(entitylivingbaseIn, 0.0F), ColorUtil.packColor(1.0F, 1.0F, 1.0F, alpha));
            }
        }
    }
}


