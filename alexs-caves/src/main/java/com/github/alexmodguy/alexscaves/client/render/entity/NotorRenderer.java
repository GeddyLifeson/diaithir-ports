package com.github.alexmodguy.alexscaves.client.render.entity;

import com.github.alexthe666.citadel.client.render.legacy.LegacyMobRenderer;
import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.github.alexmodguy.alexscaves.client.ClientProxy;
import com.github.alexmodguy.alexscaves.client.model.*;
import com.github.alexmodguy.alexscaves.client.render.ColorUtil;
import com.github.alexmodguy.alexscaves.client.render.ACRenderTypes;
import com.github.alexmodguy.alexscaves.server.entity.living.*;
import com.github.alexmodguy.alexscaves.server.misc.ACMath;
import com.github.alexthe666.citadel.client.shader.PostEffectRegistry;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.*;
import com.github.alexthe666.citadel.client.render.legacy.LegacyLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;


public class NotorRenderer extends LegacyMobRenderer<NotorEntity, NotorModel> {
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/notor.png");
    private static final Identifier TEXTURE_GLOW = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/notor_glow.png");
    private static final Identifier TEXTURE_EYES = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/notor_eyes.png");
    private static final List<NotorEntity> allOnScreen = new ArrayList<>();

    public NotorRenderer(EntityRendererProvider.Context renderManagerIn) {
        super(renderManagerIn, new NotorModel(), 0.25F);
        this.addLayer(new NotorRenderer.LayerGlow());
    }

    public boolean shouldRender(NotorEntity entity, Frustum camera, double x, double y, double z) {
        if (super.shouldRender(entity, camera, x, y, z)) {
            return true;
        } else {
            if (entity.getBeamProgress(1.0F) > 0) {
                Vec3 vec3 = entity.position();
                Vec3 vec31 = entity.getBeamEndPosition(1.0F);
                return camera.isVisible(new AABB(vec31.x, vec31.y, vec31.z, vec3.x, vec3.y, vec3.z));
            }
            return false;
        }
    }

    protected void scale(NotorEntity mob, PoseStack matrixStackIn, float partialTicks) {
    }

    public void render(NotorEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource source, int packedLight) {
        super.render(entity, entityYaw, partialTicks, poseStack, source, packedLight);
        Vec3 renderAt = entity.getPosition(partialTicks);
        if (entity.isAlive()) {
            Entity hologramEntity = entity.getHologramEntity();
            boolean scanning = !entity.showingHologram();
            Vec3 hologramScanPos = entity.getBeamEndPosition(partialTicks);
            float beamProgress = entity.getBeamProgress(partialTicks);
            if (hologramEntity != null && entity.showingHologram()) {
                PostEffectRegistry.renderEffectForNextTick(ClientProxy.HOLOGRAM_SHADER);
                poseStack.pushPose();
                poseStack.translate(hologramScanPos.x - renderAt.x, hologramScanPos.y - renderAt.y, hologramScanPos.z - renderAt.z);
                poseStack.scale(1F, entity.getHologramProgress(partialTicks), 1F);
                poseStack.mulPose(Axis.YP.rotationDegrees((entity.tickCount + partialTicks) * 3F));
                renderEntityInHologram(hologramEntity, 0, 0, 0, 0, partialTicks, poseStack, source, 240);
                poseStack.popPose();
            }
            if (hologramScanPos != null) {
                Vec3 eyeOffset = entity.getViewVector(1.0F).scale(0.1F);
                Vec3 modelOffset = model.getChainPosition(Vec3.ZERO).add(eyeOffset);
                Vec3 toTranslate = hologramScanPos.subtract(entity.getPosition(partialTicks).add(modelOffset));
                float yRot = ((float) Mth.atan2(toTranslate.x, toTranslate.z)) * 180.0F / (float) Math.PI;
                float xRot = -(float) (Mth.atan2(toTranslate.y, toTranslate.horizontalDistance()) * (double) (180F / (float) Math.PI));
                float length = ((float) toTranslate.length() - (scanning ? 0 : 0)) * beamProgress;
                float width = hologramEntity == null ? 1.3F : hologramEntity.getBbHeight() / 2F;
                poseStack.pushPose();
                poseStack.translate(modelOffset.x, modelOffset.y, modelOffset.z);
                poseStack.mulPose(Axis.YP.rotationDegrees(yRot - 90));
                poseStack.mulPose(Axis.ZN.rotationDegrees(xRot));
                poseStack.mulPose(Axis.ZN.rotationDegrees(90));
                if (scanning) {
                    poseStack.mulPose(Axis.YN.rotationDegrees(90));
                }
                PoseStack.Pose posestack$pose = poseStack.last();
                Matrix4f matrix4f1 = posestack$pose.pose();
                PostEffectRegistry.renderEffectForNextTick(ClientProxy.HOLOGRAM_SHADER);
                VertexConsumer lightConsumer = source.getBuffer(ACRenderTypes.getHologramLights());
                shineOriginVertex(lightConsumer, matrix4f1, 0, 0);
                shineLeftCornerVertex(lightConsumer, matrix4f1, length, width, 0, 0);
                shineRightCornerVertex(lightConsumer, matrix4f1, length, width, 0, 0);
                shineLeftCornerVertex(lightConsumer, matrix4f1, length, width, 0, 0);
                poseStack.popPose();
            }
        }

    }

    public static <E extends Entity> void renderEntityInHologram(E entityIn, double x, double y, double z, float yaw, float partialTicks, PoseStack matrixStack, MultiBufferSource bufferIn, int packedLight) {
        com.github.alexthe666.citadel.client.render.legacy.LegacyRenderContext.renderEntity(entityIn, x, y, z, yaw, partialTicks, matrixStack, packedLight);
    }

    private static void shineOriginVertex(VertexConsumer p_114220_, Matrix4f p_114221_, float xOffset, float yOffset) {
        p_114220_.addVertex(p_114221_, 0.0F, 0.0F, 0.0F).setColor(255, 255, 255, 230);
    }

    private static void shineLeftCornerVertex(VertexConsumer p_114215_, Matrix4f p_114216_, float p_114217_, float p_114218_, float xOffset, float yOffset) {
        p_114215_.addVertex(p_114216_, -ACMath.HALF_SQRT_3 * p_114218_, p_114217_, 0).setColor(0, 0, 255, 0);
    }

    private static void shineRightCornerVertex(VertexConsumer p_114224_, Matrix4f p_114225_, float p_114226_, float p_114227_, float xOffset, float yOffset) {
        p_114224_.addVertex(p_114225_, ACMath.HALF_SQRT_3 * p_114227_, p_114226_, 0).setColor(0, 0, 255, 0);
    }


    public Identifier getTextureLocation(NotorEntity entity) {
        return TEXTURE;
    }

    class LayerGlow extends LegacyLayer<NotorEntity, NotorModel> {

        public LayerGlow() {
            super(NotorRenderer.this);
        }

        public void render(PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, NotorEntity entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
            VertexConsumer ivertexbuilder = bufferIn.getBuffer(ACRenderTypes.getGhostly(TEXTURE_GLOW));
            float alpha = 1F;
            this.getParentModel().renderToBuffer(matrixStackIn, ivertexbuilder, packedLightIn, com.github.alexthe666.citadel.client.render.legacy.LegacyMobRenderer.getOverlayCoords(entitylivingbaseIn, 0.0F), ColorUtil.packColor(1.0F, 1.0F, 1.0F, alpha));
            VertexConsumer ivertexbuilder2;
            if (entitylivingbaseIn.getBeamProgress(partialTicks) > 0) {
                PostEffectRegistry.renderEffectForNextTick(ClientProxy.HOLOGRAM_SHADER);
                ivertexbuilder2 = bufferIn.getBuffer(ACRenderTypes.getHologram(TEXTURE_EYES));
            } else {
                ivertexbuilder2 = bufferIn.getBuffer(RenderTypes.eyes(TEXTURE_EYES));
            }
            this.getParentModel().renderToBuffer(matrixStackIn, ivertexbuilder2, packedLightIn, com.github.alexthe666.citadel.client.render.legacy.LegacyMobRenderer.getOverlayCoords(entitylivingbaseIn, 0.0F), ColorUtil.packColor(1.0F, 1.0F, 1.0F, 1.0F));

        }
    }
}


