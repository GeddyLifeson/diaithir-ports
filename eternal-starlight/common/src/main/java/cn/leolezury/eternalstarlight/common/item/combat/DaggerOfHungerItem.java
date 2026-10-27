package cn.leolezury.eternalstarlight.common.item.combat;

import cn.leolezury.eternalstarlight.common.util.ESPortCompat;

import cn.leolezury.eternalstarlight.common.data.ESDamageTypes;
import cn.leolezury.eternalstarlight.common.registry.ESCriteriaTriggers;
import cn.leolezury.eternalstarlight.common.registry.ESDataComponents;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import cn.leolezury.eternalstarlight.common.item.combat.ESItemTiers;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.stream.Collectors;

public class DaggerOfHungerItem extends DualWieldingSwordItem {
	public static final ItemAttributeModifiers DEFAULT_ATTRIBUTE = ESPortCompat.baseAttributes(ESItemTiers.TOOTH_OF_HUNGER, 3, -2.4f);
	public static final ItemAttributeModifiers BONUS_ATTRIBUTE = ESPortCompat.baseAttributes(ESItemTiers.TOOTH_OF_HUNGER, 4, -1.9f);
	public static final ItemAttributeModifiers PENALTY_ATTRIBUTE = ESPortCompat.baseAttributes(ESItemTiers.TOOTH_OF_HUNGER, 2, -2.9f);

	public DaggerOfHungerItem(ESItemTiers tier, Properties properties) {
		super(tier, properties);
		// STUB: durability suppression hack removed (components field is gone in 26.1)
	}

	@Override
	public void postHurtEnemy(ItemStack stack, LivingEntity entity, LivingEntity attacker) {
		super.postHurtEnemy(stack, entity, attacker);
		entity.addEffect(new MobEffectInstance(MobEffects.HUNGER, 60));
		if (attacker instanceof Player player) {
			player.getFoodData().eat(3, 0);
		}
		float hungerLevel = Mth.clamp(stack.getOrDefault(ESDataComponents.HUNGER_LEVEL.get(), 0f), -1, 1);
		float newHungerLevel = Math.min(1, hungerLevel + 0.05f);
		stack.applyComponentsAndValidate(DataComponentPatch.builder().set(ESDataComponents.HUNGER_LEVEL.get(), newHungerLevel).build());
		if (newHungerLevel == 1 && attacker instanceof ServerPlayer player) {
			ESCriteriaTriggers.SATURATE_DAGGER_OF_HUNGER.get().trigger(player);
		}
	}

	@Override
	public void inventoryTick(ItemStack stack, net.minecraft.server.level.ServerLevel level, Entity entity, @Nullable net.minecraft.world.entity.EquipmentSlot slot) {
		if (entity.tickCount % 1200 == 0) {
			float hungerLevel = Mth.clamp(stack.getOrDefault(ESDataComponents.HUNGER_LEVEL.get(), 0f), -1, 1);
			float newHungerLevel = Math.max(-1, hungerLevel - 0.00005f * 1200);
			stack.applyComponentsAndValidate(DataComponentPatch.builder().set(ESDataComponents.HUNGER_LEVEL.get(), newHungerLevel).build());
			if (hungerLevel == -1) {
				ESPortCompat.hurt(entity, ESDamageTypes.getDamageSource(level, ESDamageTypes.DAGGER_OF_HUNGER), 3);
				newHungerLevel = Math.min(1, newHungerLevel + 0.05f);
				stack.applyComponentsAndValidate(DataComponentPatch.builder().set(ESDataComponents.HUNGER_LEVEL.get(), newHungerLevel).build());
			}
		}
		if (entity.tickCount % 600 == 0) {
			float hungerLevel = Mth.clamp(stack.getOrDefault(ESDataComponents.HUNGER_LEVEL.get(), 0f), -1, 1);
			int state = Math.min(2, (int) ((hungerLevel + 1f) * 1.5f));
			ItemAttributeModifiers modifiers = switch (state) {
				case 0 -> PENALTY_ATTRIBUTE;
				case 2 -> BONUS_ATTRIBUTE;
				default -> DEFAULT_ATTRIBUTE;
			};
			stack.applyComponentsAndValidate(DataComponentPatch.builder().set(DataComponents.ATTRIBUTE_MODIFIERS, modifiers).build());
		}
		super.inventoryTick(stack, level, entity, slot);
	}

	@Override
	public boolean isBarVisible(ItemStack stack) {
		float hungerLevel = Mth.clamp(stack.getOrDefault(ESDataComponents.HUNGER_LEVEL.get(), 0f), -1, 1);
		return hungerLevel < 1;
	}

	@Override
	public int getBarWidth(ItemStack stack) {
		float hungerLevel = Mth.clamp(stack.getOrDefault(ESDataComponents.HUNGER_LEVEL.get(), 0f), -1, 1);
		return Mth.clamp(Math.round(13.0F - (1.0F - hungerLevel) * 13.0F / 2.0F), 0, 13);
	}

	@Override
	public int getBarColor(ItemStack stack) {
		float hungerLevel = Mth.clamp(stack.getOrDefault(ESDataComponents.HUNGER_LEVEL.get(), 0f), -1, 1);
		float progress = Mth.clamp((hungerLevel + 1.0F) / 2.0F, 0, 1);
		return ARGB.colorFromFloat(1, 103.0F * progress / 255.0F, 47.0F * progress / 255.0F, 207.0F * progress / 255.0F);
	}
}
