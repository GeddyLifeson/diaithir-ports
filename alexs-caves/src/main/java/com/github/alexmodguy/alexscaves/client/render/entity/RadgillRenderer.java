package com.github.alexmodguy.alexscaves.client.render.entity;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.github.alexmodguy.alexscaves.client.model.RadgillModel;
import com.github.alexmodguy.alexscaves.server.entity.living.RadgillEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import com.github.alexthe666.citadel.client.render.legacy.LegacyMobRenderer;
import com.github.alexthe666.citadel.client.render.legacy.LegacyLayer;
import net.minecraft.resources.Identifier;

public class RadgillRenderer extends LegacyMobRenderer<RadgillEntity, RadgillModel> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/radgill.png");
    private static final Identifier TEXTURE_EYES = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/radgill_eyes.png");

    public RadgillRenderer(EntityRendererProvider.Context renderManagerIn) {
        super(renderManagerIn, new RadgillModel(), 0.25F);
        this.addLayer(new LayerGlow());
    }

    protected void scale(RadgillEntity mob, PoseStack matrixStackIn, float partialTicks) {
        matrixStackIn.scale(0.9F, 0.9F, 0.9F);
    }

    public Identifier getTextureLocation(RadgillEntity entity) {
        return TEXTURE;
    }

    class LayerGlow extends LegacyLayer<RadgillEntity, RadgillModel> {

        public LayerGlow() {
            super(RadgillRenderer.this);
        }

        public void render(PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, RadgillEntity entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
            VertexConsumer ivertexbuilder = bufferIn.getBuffer(RenderTypes.eyes(TEXTURE_EYES));
            float alpha = 1.0F;
            this.getParentModel().renderToBuffer(matrixStackIn, ivertexbuilder, packedLightIn, com.github.alexthe666.citadel.client.render.legacy.LegacyMobRenderer.getOverlayCoords(entitylivingbaseIn, 0.0F), -1);
        }
    }
}


