package net.diaithir.druidry.ars.api.client;

import net.minecraft.world.item.ItemStack;

/** Marker for items that show the mana bar while held. */
public interface IDisplayMana {
    default boolean shouldDisplayMana(ItemStack stack) {
        return true;
    }
}
