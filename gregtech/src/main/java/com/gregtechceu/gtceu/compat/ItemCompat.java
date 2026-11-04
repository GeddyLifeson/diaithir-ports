package com.gregtechceu.gtceu.compat;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.Tag;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/** Helpers for ItemStack methods that were removed in 26.1. */
public final class ItemCompat {

    private ItemCompat() {}

    public static Tag save(ItemStack stack, HolderLookup.Provider provider) {
        return NbtCompat.saveItem(provider, stack);
    }

    public static String getDescriptionId(ItemStack stack) {
        return stack.getItem().getDescriptionId();
    }

    public static FoodProperties getFoodProperties(ItemStack stack, LivingEntity entity) {
        return stack.get(DataComponents.FOOD);
    }
}
