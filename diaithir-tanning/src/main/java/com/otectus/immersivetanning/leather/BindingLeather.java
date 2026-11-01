package com.otectus.immersivetanning.leather;

import com.otectus.immersivetanning.registry.ModItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Binding Leather takes vanilla leather's place where it is dropped: loot and mob drops hand out Binding Leather, and
 * recipes, trades and the leather conversion recipe that need leather are data-driven overrides in the datapack.
 */
public final class BindingLeather {

    /** The stack itself, or Binding Leather in its place if it is vanilla leather. */
    public static ItemStack replace(ItemStack stack) {
        if (!stack.is(Items.LEATHER)) return stack;
        ItemStack out = new ItemStack(ModItems.BINDING_LEATHER.get(), stack.getCount());
        out.applyComponents(stack.getComponentsPatch());
        return out;
    }

    private BindingLeather() {}
}
