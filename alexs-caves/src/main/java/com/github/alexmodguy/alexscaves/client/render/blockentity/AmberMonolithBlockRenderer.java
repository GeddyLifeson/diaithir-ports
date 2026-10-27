package com.github.alexmodguy.alexscaves.client.render.blockentity;

import com.github.alexmodguy.alexscaves.client.model.SauropodBaseModel;
import com.github.alexmodguy.alexscaves.client.render.ColorUtil;
import com.github.alexmodguy.alexscaves.server.block.blockentity.AmberMonolithBlockEntity;
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
import com.github.alexthe666.citadel.client.render.legacy.LegacyBlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class AmberMonolithBlockRenderer<T extends AmberMonolithBlockEntity> extends LegacyBlockEntityRenderer<T> {


    protected final RandomSource random = RandomSource.create();

    public AmberMonolithBlockRenderer(BlockEntityRendererProvider.Context rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }

    @Override
    public void render(T amber, float partialTicks, PoseStack poseStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
        Entity currentEntity = amber.getDisplayEntity(Minecraft.getInstance().level);
        float age = amber.tickCount + partialTicks;
        float spin = amber.getRotation(partialTicks);
        poseStack.pushPose();
        poseStack.translate(0.5F, 1.65F, 0.5F);
        if (currentEntity != null) {
            float f = 0.45F;
            float f1 = Math.max(currentEntity.getBbWidth(), currentEntity.getBbHeight());
            if ((double) f1 > 1.0D) {
                f /= f1 * 1.5F;
            }
            poseStack.translate(0, f * 1.5F - 1.25F + (float) (Math.cos(age * 0.05) * 0.05F), 0);
            poseStack.scale(f, f, f);
            poseStack.mulPose(Axis.YP.rotationDegrees(spin));
            renderEntityInAmber(currentEntity, 0, 0, 0, 0, partialTicks, poseStack, bufferIn, 1.0F);
        }
        poseStack.popPose();
    }

    public static <E extends Entity> void renderEntityInAmber(E entityIn, double x, double y, double z, float yaw, float partialTicks, PoseStack matrixStack, MultiBufferSource bufferIn, float transparency) {
        com.github.alexthe666.citadel.client.render.legacy.LegacyRenderContext.renderEntity(entityIn, x, y, z, yaw, partialTicks, matrixStack, 15728880);
    }
}
