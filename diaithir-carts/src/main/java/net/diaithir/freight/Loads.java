package net.diaithir.freight;

import net.minecraft.world.item.ItemStack;

import java.util.List;

/** Mass of loose items. Items carry no mass of their own, so every item counts a quarter kilogram. */
public final class Loads {
    public static final double ITEM_KG = 0.25;

    private Loads() {}

    public static double kg(List<ItemStack> items) {
        long n = 0;
        for (ItemStack s : items) n += s.getCount();
        return n * ITEM_KG;
    }
}
