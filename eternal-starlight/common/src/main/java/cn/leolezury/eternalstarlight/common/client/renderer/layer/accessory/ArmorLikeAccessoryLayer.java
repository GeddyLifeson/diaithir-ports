package cn.leolezury.eternalstarlight.common.client.renderer.layer.accessory;

import cn.leolezury.eternalstarlight.common.EternalStarlight;
import cn.leolezury.eternalstarlight.common.client.compat.ESRender;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * Accessory layer drawn like armor on any humanoid renderer. The wearer is looked up through {@link ESRender#entityOf}.
 */
public abstract class ArmorLikeAccessoryLayer extends RenderLayer<HumanoidRenderState, HumanoidModel<HumanoidRenderState>> {
	public static final ModelLayerLocation INNER_LOCATION = new ModelLayerLocation(EternalStarlight.id("accessory"), "inner_armor");
	public static final ModelLayerLocation OUTER_LOCATION = new ModelLayerLocation(EternalStarlight.id("accessory"), "outer_armor");

	private final HumanoidModel<HumanoidRenderState> innerModel;
	private final HumanoidModel<HumanoidRenderState> outerModel;

	public ArmorLikeAccessoryLayer(RenderLayerParent<HumanoidRenderState, HumanoidModel<HumanoidRenderState>> renderer, HumanoidModel<HumanoidRenderState> innerModel, HumanoidModel<HumanoidRenderState> outerModel) {
		super(renderer);
		this.innerModel = innerModel;
		this.outerModel = outerModel;
	}

	@Override
	public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, HumanoidRenderState state, float yRot, float xRot) {
		Entity entity = ESRender.entityOf(state);
		if (entity instanceof LivingEntity livingEntity && shouldRender(livingEntity)) {
			for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET, EquipmentSlot.HEAD}) {
				this.renderArmorLikePiece(poseStack, collector, livingEntity, state, slot, lightCoords);
			}
		}
	}

	private void renderArmorLikePiece(PoseStack poseStack, SubmitNodeCollector collector, LivingEntity livingEntity, HumanoidRenderState state, EquipmentSlot slot, int lightCoords) {
		Identifier texture = getAccessoryTexture(livingEntity, livingEntity.getItemBySlot(slot), slot);
		if (texture != null) {
			HumanoidModel<HumanoidRenderState> model = this.usesInnerModel(slot) ? this.innerModel : this.outerModel;
			model.setupAnim(state);
			this.setPartVisibility(model, slot);
			collector.submitModel(model, state, poseStack, texture, lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor, null);
		}
	}

	private static void setAllVisible(HumanoidModel<?> model, boolean visible) {
		for (ModelPart part : new ModelPart[]{model.head, model.hat, model.body, model.rightArm, model.leftArm, model.rightLeg, model.leftLeg}) {
			part.visible = visible;
		}
	}

	protected void setPartVisibility(HumanoidModel<HumanoidRenderState> model, EquipmentSlot slot) {
		setAllVisible(model, false);
		switch (slot) {
			case HEAD:
				model.head.visible = true;
				model.hat.visible = true;
				break;
			case CHEST:
				model.body.visible = true;
				model.rightArm.visible = true;
				model.leftArm.visible = true;
				break;
			case LEGS:
				model.body.visible = true;
				model.rightLeg.visible = true;
				model.leftLeg.visible = true;
				break;
			case FEET:
				model.rightLeg.visible = true;
				model.leftLeg.visible = true;
		}
	}

	protected boolean usesInnerModel(EquipmentSlot slot) {
		return slot == EquipmentSlot.LEGS;
	}

	protected abstract Identifier getAccessoryTexture(LivingEntity entity, ItemStack stack, EquipmentSlot slot);

	protected abstract boolean shouldRender(LivingEntity entity);
}
