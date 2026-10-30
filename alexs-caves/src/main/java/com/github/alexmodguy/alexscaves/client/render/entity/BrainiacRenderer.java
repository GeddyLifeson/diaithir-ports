package com.github.alexmodguy.alexscaves.client.render.entity;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.github.alexmodguy.alexscaves.client.model.BrainiacModel;
import com.github.alexmodguy.alexscaves.client.render.ColorUtil;
import com.github.alexmodguy.alexscaves.client.render.entity.layer.BrainiacBackBarrelLayer;
import com.github.alexmodguy.alexscaves.server.entity.living.BrainiacEntity;
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

public class BrainiacRenderer extends LegacyMobRenderer<BrainiacEntity, BrainiacModel> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/brainiac.png");
    private static final Identifier TEXTURE_EYES = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/brainiac_glow.png");

    public BrainiacRenderer(EntityRendererProvider.Context renderManagerIn) {
        super(renderManagerIn, new BrainiacModel(), 0.25F);
        this.addLayer(new BrainiacBackBarrelLayer(this));
        this.addLayer(new LayerGlow());
    }

    protected void scale(RadgillEntity mob, PoseStack matrixStackIn, float partialTicks) {
        matrixStackIn.scale(0.9F, 0.9F, 0.9F);
    }

    public Identifier getTextureLocation(BrainiacEntity entity) {
        return TEXTURE;
    }

    class LayerGlow extends LegacyLayer<BrainiacEntity, BrainiacModel> {

        public LayerGlow() {
            super(BrainiacRenderer.this);
        }

        public void render(PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, BrainiacEntity entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
            VertexConsumer ivertexbuilder = bufferIn.getBuffer(RenderTypes.eyes(TEXTURE_EYES));
            float alpha = 1.0F;
            this.getParentModel().renderToBuffer(matrixStackIn, ivertexbuilder, packedLightIn, com.github.alexthe666.citadel.client.render.legacy.LegacyMobRenderer.getOverlayCoords(entitylivingbaseIn, 0.0F), ColorUtil.packColor(1.0F, 1.0F, 1.0F, alpha));
        }
    }
}


