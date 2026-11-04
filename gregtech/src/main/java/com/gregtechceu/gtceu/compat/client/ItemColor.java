package com.gregtechceu.gtceu.compat.client;

import net.minecraft.world.item.ItemStack;

/** Replacement for the removed 1.21 item colour handler; tint sources replace it in 26.1. */
@FunctionalInterface
public interface ItemColor {
    int getColor(ItemStack stack, int tintIndex);
}
