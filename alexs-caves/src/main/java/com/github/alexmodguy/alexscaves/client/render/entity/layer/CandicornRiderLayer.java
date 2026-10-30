package com.github.alexmodguy.alexscaves.client.render.entity.layer;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.github.alexmodguy.alexscaves.client.model.CandicornModel;
import com.github.alexmodguy.alexscaves.client.render.entity.CandicornRenderer;
import com.github.alexmodguy.alexscaves.server.entity.living.CandicornEntity;
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

public class CandicornRiderLayer extends LegacyLayer<CandicornEntity, CandicornModel> {

    public CandicornRiderLayer(CandicornRenderer render) {
        super(render);
    }

    public void render(PoseStack poseStack, MultiBufferSource bufferIn, int packedLightIn, CandicornEntity entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        float bodyYaw = entity.yBodyRotO + (entity.yBodyRot - entity.yBodyRotO) * partialTicks;
        if (entity.isVehicle()) {
            for (Entity passenger : entity.getPassengers()) {
                if (passenger == Minecraft.getInstance().player && Minecraft.getInstance().options.getCameraType().isFirstPerson()) {
                    continue;
                }
                poseStack.pushPose();
                getParentModel().translateToSaddle(poseStack);
                poseStack.translate(0, 0.55F, 0.1F);
                poseStack.mulPose(Axis.XN.rotationDegrees(180F));
                poseStack.mulPose(Axis.YN.rotationDegrees(360 - bodyYaw));
                AlexsCaves.PROXY.releaseRenderingEntity(passenger.getUUID());
                renderPassenger(passenger, 0, 0, 0, 0, partialTicks, poseStack, bufferIn, packedLightIn);
                AlexsCaves.PROXY.blockRenderingEntity(passenger.getUUID());
                poseStack.popPose();
            }

        }
    }

    public static <E extends Entity> void renderPassenger(E entityIn, double x, double y, double z, float yaw, float partialTicks, PoseStack matrixStack, MultiBufferSource bufferIn, int packedLight) {
        com.github.alexthe666.citadel.client.render.legacy.LegacyRenderContext.renderEntity(entityIn, x, y, z, yaw, partialTicks, matrixStack, packedLight);
    }

}
