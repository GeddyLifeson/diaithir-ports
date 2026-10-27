package cn.leolezury.eternalstarlight.common.client.renderer.layer;

import cn.leolezury.eternalstarlight.common.client.compat.ESRender;
import cn.leolezury.eternalstarlight.common.client.compat.ESRenderLayer;
import cn.leolezury.eternalstarlight.common.client.compat.ESRenderState;
import cn.leolezury.eternalstarlight.common.client.model.entity.TheGatekeeperModel;
import cn.leolezury.eternalstarlight.common.entity.living.boss.gatekeeper.TheGatekeeper;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Stand-in for ItemInHandLayer, which only works with vanilla render states.
 */
public class GatekeeperHandItemLayer<T extends TheGatekeeper> extends ESRenderLayer<T, TheGatekeeperModel<T>> {
	public GatekeeperHandItemLayer(RenderLayerParent<ESRenderState, TheGatekeeperModel<T>> parent) {
		super(parent);
	}

	@Override
	public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T entity, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
		HumanoidArm mainArm = entity.getMainArm();
		renderArm(entity, mainArm, entity.getMainHandItem(), poseStack, packedLight);
		renderArm(entity, mainArm.getOpposite(), entity.getOffhandItem(), poseStack, packedLight);
	}

	private void renderArm(T entity, HumanoidArm arm, ItemStack stack, PoseStack poseStack, int packedLight) {
		if (stack.isEmpty()) return;
		poseStack.pushPose();
		getParentModel().translateToHand(this.state, arm, poseStack);
		poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
		poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
		boolean left = arm == HumanoidArm.LEFT;
		poseStack.translate((float) (left ? -1 : 1) / 16.0F, 0.125F, -0.625F);
		ESRender.submitItem(this.collector, stack, left ? ItemDisplayContext.THIRD_PERSON_LEFT_HAND : ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, poseStack, packedLight, OverlayTexture.NO_OVERLAY, entity.level(), entity, entity.getId());
		poseStack.popPose();
	}
}
