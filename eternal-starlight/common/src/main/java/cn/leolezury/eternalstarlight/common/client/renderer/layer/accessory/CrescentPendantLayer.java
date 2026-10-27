package cn.leolezury.eternalstarlight.common.client.renderer.layer.accessory;

import cn.leolezury.eternalstarlight.common.client.compat.ESRenderState;
import cn.leolezury.eternalstarlight.common.EternalStarlight;
import cn.leolezury.eternalstarlight.common.registry.ESItems;
import cn.leolezury.eternalstarlight.common.util.ESAccessoryUtil;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class CrescentPendantLayer extends ArmorLikeAccessoryLayer<LivingEntity, HumanoidModel<LivingEntity>, HumanoidModel<LivingEntity>> {
	private static final Identifier TEXTURE = EternalStarlight.id("textures/accessory/crescent_pendant.png");

	public CrescentPendantLayer(RenderLayerParent<ESRenderState, HumanoidModel<LivingEntity>> renderer, HumanoidModel<LivingEntity> innerModel, HumanoidModel<LivingEntity> outerModel) {
		super(renderer, innerModel, outerModel);
	}

	@Override
	protected Identifier getAccessoryTexture(LivingEntity entity, ItemStack stack, EquipmentSlot slot) {
		return slot == EquipmentSlot.HEAD || slot == EquipmentSlot.CHEST ? TEXTURE : null;
	}

	@Override
	protected boolean shouldRender(LivingEntity entity) {
		return ESAccessoryUtil.getActiveAccessoriesOnArmors(entity).contains(ESItems.CRESCENT_PENDANT.get());
	}
}
