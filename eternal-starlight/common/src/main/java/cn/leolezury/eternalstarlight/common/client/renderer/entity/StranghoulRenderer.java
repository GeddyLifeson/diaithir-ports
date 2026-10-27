package cn.leolezury.eternalstarlight.common.client.renderer.entity;

import cn.leolezury.eternalstarlight.common.EternalStarlight;
import cn.leolezury.eternalstarlight.common.client.model.entity.StranghoulModel;
import cn.leolezury.eternalstarlight.common.client.renderer.state.StranghoulRenderState;
import cn.leolezury.eternalstarlight.common.entity.living.monster.Stranghoul;
import cn.leolezury.eternalstarlight.common.item.combat.SeedsLauncherItem;
import cn.leolezury.eternalstarlight.common.item.combat.SpearItem;
import cn.leolezury.eternalstarlight.common.platform.ESPlatform;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class StranghoulRenderer extends HumanoidMobRenderer<Stranghoul, StranghoulRenderState, StranghoulModel> {
	private static final Identifier ENTITY_TEXTURE = EternalStarlight.id("textures/entity/stranghoul.png");

	public StranghoulRenderer(EntityRendererProvider.Context context) {
		super(context, new StranghoulModel(context.bakeLayer(StranghoulModel.LAYER_LOCATION)), 0.5f);
		this.addLayer(new HumanoidArmorLayer<>(this, ArmorModelSet.bake(StranghoulModel.ARMOR_LOCATIONS, context.getModelSet(), HumanoidModel<StranghoulRenderState>::new), context.getEquipmentRenderer()));
	}

	@Override
	public StranghoulRenderState createRenderState() {
		return new StranghoulRenderState();
	}

	@Override
	public void extractRenderState(Stranghoul entity, StranghoulRenderState state, float partialTicks) {
		super.extractRenderState(entity, state, partialTicks);
		state.isAggressive = entity.isAggressive();
		state.weaponEmpty = entity.getWeaponItem().isEmpty();
		state.eating = entity.isEating();
		state.bartering = entity.isBartering();
	}

	// from DrownedModel & SkeletonModel
	@Override
	protected HumanoidModel.ArmPose getArmPose(Stranghoul entity, HumanoidArm arm) {
		ItemStack stack = entity.getItemInHand(InteractionHand.MAIN_HAND);
		if (entity.getMainArm() == arm && entity.isAggressive()) {
			if (stack.getItem() instanceof SpearItem) {
				return HumanoidModel.ArmPose.SPEAR;
			}
			if (stack.getItem() instanceof SeedsLauncherItem
				|| (ESPlatform.INSTANCE.getLoader() == ESPlatform.Loader.FABRIC && stack.is(Items.BOW))
				|| (ESPlatform.INSTANCE.getLoader() == ESPlatform.Loader.NEOFORGE && stack.getItem() instanceof BowItem)) {
				return HumanoidModel.ArmPose.BOW_AND_ARROW;
			}
		}
		return super.getArmPose(entity, arm);
	}

	@Override
	public Identifier getTextureLocation(StranghoulRenderState state) {
		return ENTITY_TEXTURE;
	}
}
