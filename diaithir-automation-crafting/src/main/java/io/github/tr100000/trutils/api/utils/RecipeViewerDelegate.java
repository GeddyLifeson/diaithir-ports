package io.github.tr100000.trutils.api.utils;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public interface RecipeViewerDelegate {
    None NONE = new None();

    default boolean showAllRecipes() { return false; }
    default boolean showRecipe(Identifier id) { return false; }
    default boolean showRecipes(ItemStack stack) { return false; }
    default boolean showUses(ItemStack stack) { return false; }

    final class None implements RecipeViewerDelegate {
        private None() {}
    }
}
