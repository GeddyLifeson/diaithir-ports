package com.github.alexmodguy.alexscaves.client.render.entity;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.github.alexmodguy.alexscaves.client.model.ForsakenModel;
import com.github.alexmodguy.alexscaves.client.render.ColorUtil;
import com.github.alexmodguy.alexscaves.client.render.entity.layer.ForsakenHeldMobLayer;
import com.github.alexmodguy.alexscaves.server.entity.living.ForsakenEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import com.github.alexthe666.citadel.client.render.legacy.LegacyMobRenderer;
import com.github.alexthe666.citadel.client.render.legacy.LegacyLayer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;

public class ForsakenRenderer extends LegacyMobRenderer<ForsakenEntity, ForsakenModel> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/forsaken.png");
    private static final Identifier TEXTURE_EYES = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/forsaken_eyes.png");

    private static final Identifier TEXTURE_DARKNESS = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/forsaken_darkness.png");

    private static final HashMap<Integer, Vec3> mouthParticlePositions = new HashMap<>();

    public ForsakenRenderer(EntityRendererProvider.Context renderManagerIn) {
        super(renderManagerIn, new ForsakenModel(), 1.15F);
        this.addLayer(new LayerGlow());
        this.addLayer(new ForsakenHeldMobLayer(this));
    }

    

    public void render(ForsakenEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource source, int packedLight) {
        super.render(entity, entityYaw, partialTicks, poseStack, source, packedLight);
        mouthParticlePositions.put(entity.getId(), this.model.getMouthPosition(Vec3.ZERO));
    }

    public static Vec3 getMouthPositionFor(int entityId) {
        return mouthParticlePositions.get(entityId);
    }

    public Identifier getTextureLocation(ForsakenEntity entity) {
        return TEXTURE;
    }

    class LayerGlow extends LegacyLayer<ForsakenEntity, ForsakenModel> {

        public LayerGlow() {
            super(ForsakenRenderer.this);
        }

        public void render(PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, ForsakenEntity entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
            VertexConsumer darnkessConsumer = bufferIn.getBuffer(RenderTypes.entityTranslucent(TEXTURE_DARKNESS));
            this.getParentModel().renderToBuffer(matrixStackIn, darnkessConsumer, packedLightIn, com.github.alexthe666.citadel.client.render.legacy.LegacyMobRenderer.getOverlayCoords(entitylivingbaseIn, 0.0F), ColorUtil.packColor(1.0F, 1.0F, 1.0F, entitylivingbaseIn.getDarknessAmount(partialTicks)));
            VertexConsumer eyesConsumer = bufferIn.getBuffer(RenderTypes.eyes(TEXTURE_EYES));
            this.getParentModel().renderToBuffer(matrixStackIn, eyesConsumer, packedLightIn, com.github.alexthe666.citadel.client.render.legacy.LegacyMobRenderer.getOverlayCoords(entitylivingbaseIn, 0.0F), -1);
        }
    }
}


