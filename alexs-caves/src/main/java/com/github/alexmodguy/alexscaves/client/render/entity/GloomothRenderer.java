package com.github.alexmodguy.alexscaves.client.render.entity;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.github.alexmodguy.alexscaves.client.model.GloomothModel;
import com.github.alexmodguy.alexscaves.client.render.ACRenderTypes;
import com.github.alexmodguy.alexscaves.client.render.ColorUtil;
import com.github.alexmodguy.alexscaves.server.entity.living.GloomothEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import com.github.alexthe666.citadel.client.render.legacy.LegacyMobRenderer;
import com.github.alexthe666.citadel.client.render.legacy.LegacyLayer;
import net.minecraft.resources.Identifier;

public class GloomothRenderer extends LegacyMobRenderer<GloomothEntity, GloomothModel> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(AlexsCaves.MODID,  "textures/entity/gloomoth.png");
    private static final Identifier TEXTURE_EYESPOTS = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/gloomoth_eyespots.png");

    public GloomothRenderer(EntityRendererProvider.Context renderManagerIn) {
        super(renderManagerIn, new GloomothModel(), 0.35F);
        this.addLayer(new LayerGlow());
    }

    protected void scale(GloomothEntity mob, PoseStack matrixStackIn, float partialTicks) {
    }

    public Identifier getTextureLocation(GloomothEntity entity) {
        return TEXTURE;
    }

    class LayerGlow extends LegacyLayer<GloomothEntity, GloomothModel> {

        public LayerGlow() {
            super(GloomothRenderer.this);
        }

        public void render(PoseStack poseStack, MultiBufferSource bufferIn, int packedLightIn, GloomothEntity entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
            VertexConsumer ivertexbuilder4 = bufferIn.getBuffer(ACRenderTypes.getEyesAlphaEnabled(TEXTURE_EYESPOTS));
            this.getParentModel().renderToBuffer(poseStack, ivertexbuilder4, packedLightIn, com.github.alexthe666.citadel.client.render.legacy.LegacyMobRenderer.getOverlayCoords(entitylivingbaseIn, 0.0F), ColorUtil.packColor(1.0F, 1.0F, 1.0F, 0.33F));

        }
    }
}

