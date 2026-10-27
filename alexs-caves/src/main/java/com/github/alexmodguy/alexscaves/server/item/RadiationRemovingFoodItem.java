package com.github.alexmodguy.alexscaves.server.item;

import com.github.alexmodguy.alexscaves.server.potion.ACEffectRegistry;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;

public class RadiationRemovingFoodItem extends Item {

    public RadiationRemovingFoodItem(Properties properties) {
        super(properties);
    }

    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        MobEffectInstance mobEffectInstance = livingEntity.getEffect(ACEffectRegistry.IRRADIATED);
        FoodProperties foodProperties = stack.get(net.minecraft.core.component.DataComponents.FOOD);
        if (mobEffectInstance != null && foodProperties != null) {
            float f = Math.min(livingEntity.getMaxHealth(), livingEntity.getHealth() + (float) Math.ceil(foodProperties.nutrition() * 1.5F + 1));
            livingEntity.setHealth(f);
        }
        return super.finishUsingItem(stack, level, livingEntity);
    }

    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        return this == ACItemRegistry.GREEN_SOYLENT.get() ? ItemUtils.startUsingInstantly(level, player, hand) : super.use(level, player, hand);
    }

    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return this == ACItemRegistry.GREEN_SOYLENT.get() ? ItemUseAnimation.DRINK : ItemUseAnimation.EAT;
    }


}
