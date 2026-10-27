package cn.leolezury.eternalstarlight.common.client.renderer.layer.accessory;

import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import cn.leolezury.eternalstarlight.common.EternalStarlight;
import cn.leolezury.eternalstarlight.common.registry.ESItems;
import cn.leolezury.eternalstarlight.common.util.ESAccessoryUtil;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class FungusAmuletLayer extends ArmorLikeAccessoryLayer {
	private static final Identifier TEXTURE = EternalStarlight.id("textures/accessory/fungus_amulet.png");

	public FungusAmuletLayer(RenderLayerParent<HumanoidRenderState, HumanoidModel<HumanoidRenderState>> renderer, HumanoidModel<HumanoidRenderState> innerModel, HumanoidModel<HumanoidRenderState> outerModel) {
		super(renderer, innerModel, outerModel);
	}

	@Override
	protected Identifier getAccessoryTexture(LivingEntity entity, ItemStack stack, EquipmentSlot slot) {
		return slot == EquipmentSlot.HEAD || slot == EquipmentSlot.CHEST ? TEXTURE : null;
	}

	@Override
	protected boolean shouldRender(LivingEntity entity) {
		return ESAccessoryUtil.getActiveAccessoriesOnArmors(entity).contains(ESItems.FUNGUS_AMULET.get());
	}
}
