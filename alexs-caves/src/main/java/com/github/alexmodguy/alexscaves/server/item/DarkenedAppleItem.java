package com.github.alexmodguy.alexscaves.server.item;

import com.github.alexmodguy.alexscaves.server.misc.ACAdvancementTriggerRegistry;
import com.github.alexmodguy.alexscaves.server.potion.ACEffectRegistry;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class DarkenedAppleItem extends Item {

    public DarkenedAppleItem() {
        super(com.github.alexmodguy.alexscaves.server.misc.ACProps.item().food(ACFoods.DARKENED_APPLE.food(), ACFoods.DARKENED_APPLE.consumable()).rarity(ACItemRegistry.getRarityDemonic()));
    }

    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        MobEffectInstance mobEffectInstance = livingEntity.getEffect(ACEffectRegistry.DARKNESS_INCARNATE);
        FoodProperties foodProperties = stack.get(net.minecraft.core.component.DataComponents.FOOD);
        if (mobEffectInstance != null && foodProperties != null) {
            int newDuration = mobEffectInstance.getDuration() + 600;
            MobEffectInstance newEffect = new MobEffectInstance(ACEffectRegistry.DARKNESS_INCARNATE, newDuration, mobEffectInstance.getAmplifier());
            livingEntity.forceAddEffect(newEffect, null);
            ACAdvancementTriggerRegistry.EAT_DARKENED_APPLE.get().triggerForEntity(livingEntity);
        }
        return super.finishUsingItem(stack, level, livingEntity);
    }
}
