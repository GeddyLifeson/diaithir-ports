package de.ellpeck.actuallyadditions.mod.crafting;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;

/** Shared by the keep-data crafting recipes: copy the components of the input that matches the recipe's TargetComponentIngredient. */
final class KeepData {
    private KeepData() {}

    /** @return whether a donor input was found */
    static boolean copyFromDonor(ItemStack result, Iterable<Ingredient> ingredients, CraftingInput input) {
        TargetComponentIngredient donor = null;
        for (Ingredient ingredient : ingredients) {
            if (ingredient.getCustomIngredient() instanceof TargetComponentIngredient target) {
                donor = target;
                break;
            }
        }
        if (donor == null) return false;
        for (int i = 0; i < input.size(); i++) {
            ItemStack item = input.getItem(i);
            if (!item.isEmpty() && donor.test(item)) {
                if (!item.getComponents().isEmpty()) result.applyComponents(item.getComponents());
                return true;
            }
        }
        return false;
    }
}
