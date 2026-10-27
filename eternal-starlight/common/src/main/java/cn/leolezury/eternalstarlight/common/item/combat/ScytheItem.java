package cn.leolezury.eternalstarlight.common.item.combat;

import cn.leolezury.eternalstarlight.common.EternalStarlight;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.component.ItemAttributeModifiers;

public class ScytheItem extends TieredItem {
	public ScytheItem(Tier tier, Properties properties) {
		super(tier, properties.component(DataComponents.TOOL, tier.createToolProperties(BlockTags.MINEABLE_WITH_HOE)));
	}

	public static ItemAttributeModifiers createAttributes(Tier tier, int damage, float speed, float reach, float sweep) {
		ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder()
			.add(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, (float) damage + tier.getAttackDamageBonus(), AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
			.add(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, speed, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
			.add(Attributes.ENTITY_INTERACTION_RANGE, new AttributeModifier(EternalStarlight.id("weapon.entity_reach"), reach, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
			.add(Attributes.BLOCK_INTERACTION_RANGE, new AttributeModifier(EternalStarlight.id("weapon.block_reach"), reach, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
		if (sweep > 0) {
			builder.add(Attributes.SWEEPING_DAMAGE_RATIO, new AttributeModifier(EternalStarlight.id("weapon.sweeping_damage_ratio"), sweep, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND);
		}
		return builder.build();
	}

	@Override
	public boolean hurtEnemy(ItemStack stack, LivingEntity entity, LivingEntity attacker) {
		return true;
	}

	@Override
	public void postHurtEnemy(ItemStack stack, LivingEntity entity, LivingEntity attacker) {
		stack.hurtAndBreak(1, attacker, EquipmentSlot.MAINHAND);
	}
}
