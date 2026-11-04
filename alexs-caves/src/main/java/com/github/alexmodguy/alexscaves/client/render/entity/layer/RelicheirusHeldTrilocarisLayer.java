package com.github.alexmodguy.alexscaves.client.render.entity.layer;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.github.alexmodguy.alexscaves.client.model.RelicheirusModel;
import com.github.alexmodguy.alexscaves.client.render.entity.RelicheirusRenderer;
import com.github.alexmodguy.alexscaves.server.entity.living.RelicheirusEntity;
import com.github.alexmodguy.alexscaves.server.entity.living.TrilocarisEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import com.github.alexthe666.citadel.client.render.legacy.LegacyLayer;
import net.minecraft.world.entity.Entity;

public class RelicheirusHeldTrilocarisLayer extends LegacyLayer<RelicheirusEntity, RelicheirusModel> {

    public RelicheirusHeldTrilocarisLayer(RelicheirusRenderer render) {
        super(render);
    }

    public void render(PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, RelicheirusEntity relicheirus, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        Entity heldMob = relicheirus.getHeldMob();
        if (heldMob instanceof TrilocarisEntity && relicheirus.getAnimation() == RelicheirusEntity.ANIMATION_EAT_TRILOCARIS && relicheirus.getAnimationTick() > 15) {
            float riderRot = heldMob.yRotO + (heldMob.getYRot() - heldMob.yRotO) * partialTicks;
            AlexsCaves.PROXY.releaseRenderingEntity(heldMob.getUUID());
            matrixStackIn.pushPose();
            getParentModel().translateToMouth(matrixStackIn);
            matrixStackIn.translate(0, -1.34F, -1F);
            matrixStackIn.mulPose(Axis.ZP.rotationDegrees(180F));
            matrixStackIn.mulPose(Axis.YP.rotationDegrees(riderRot + 180F));
            matrixStackIn.mulPose(Axis.YP.rotationDegrees(90F));
            matrixStackIn.translate(0, -heldMob.getBbHeight() * 0.5F, 0);
            renderEntity(heldMob, 0, 0, 0, 0, partialTicks, matrixStackIn, bufferIn, packedLightIn);
            matrixStackIn.popPose();
            AlexsCaves.PROXY.blockRenderingEntity(heldMob.getUUID());
        }
    }

    public <E extends Entity> void renderEntity(E entityIn, double x, double y, double z, float yaw, float partialTicks, PoseStack matrixStack, MultiBufferSource bufferIn, int packedLight) {
        com.github.alexthe666.citadel.client.render.legacy.LegacyRenderContext.renderEntity(entityIn, x, y, z, yaw, partialTicks, matrixStack, packedLight);
    }
}
