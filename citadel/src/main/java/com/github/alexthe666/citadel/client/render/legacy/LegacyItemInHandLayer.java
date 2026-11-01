package com.github.alexthe666.citadel.client.render.legacy;

import com.github.alexthe666.citadel.client.model.basic.BasicEntityModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/** 1.21 ItemInHandLayer for Citadel models that implement {@link LegacyArmedModel}. */
public class LegacyItemInHandLayer<T extends LivingEntity, M extends BasicEntityModel<T> & LegacyArmedModel> extends LegacyLayer<T, M> {
    public LegacyItemInHandLayer(LegacyLayerParent<T, M> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        boolean right = entity.getMainArm() == HumanoidArm.RIGHT;
        ItemStack left = right ? entity.getOffhandItem() : entity.getMainHandItem();
        ItemStack rightItem = right ? entity.getMainHandItem() : entity.getOffhandItem();
        if (left.isEmpty() && rightItem.isEmpty()) return;
        poseStack.pushPose();
        renderArmWithItem(entity, rightItem, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, HumanoidArm.RIGHT, poseStack, packedLight);
        renderArmWithItem(entity, left, ItemDisplayContext.THIRD_PERSON_LEFT_HAND, HumanoidArm.LEFT, poseStack, packedLight);
        poseStack.popPose();
    }

    protected void renderArmWithItem(T entity, ItemStack stack, ItemDisplayContext display, HumanoidArm arm, PoseStack poseStack, int light) {
        if (stack.isEmpty()) return;
        poseStack.pushPose();
        getParentModel().translateToHand(arm, poseStack);
        poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        boolean leftHand = arm == HumanoidArm.LEFT;
        poseStack.translate((float) (leftHand ? -1 : 1) / 16.0F, 0.125F, -0.625F);
        LegacyRenderContext.renderItem(entity, stack, display, poseStack, light);
        poseStack.popPose();
    }
}
