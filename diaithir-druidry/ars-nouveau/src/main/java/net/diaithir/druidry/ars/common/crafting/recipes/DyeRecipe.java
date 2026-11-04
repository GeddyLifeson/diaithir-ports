package net.diaithir.druidry.ars.common.crafting.recipes;

import net.diaithir.druidry.ars.setup.registry.RecipeRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStackTemplate;
import java.util.List;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import org.jetbrains.annotations.NotNull;

public class DyeRecipe extends ShapelessRecipe {

    public DyeRecipe(Recipe.CommonInfo commonInfo, CraftingRecipe.CraftingBookInfo bookInfo, ItemStackTemplate result, List<Ingredient> ingredients) {
        super(commonInfo, bookInfo, result, ingredients);
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull CraftingInput inv) {
        ItemStack output = super.assemble(inv);
        if (!output.isEmpty()) {
            for (int i = 0; i < inv.size(); i++) { // For each slot in the crafting inventory,
                final ItemStack ingredient = inv.getItem(i); // Get the ingredient in the slot
                if (!ingredient.isEmpty() && ingredient.getItem() instanceof IDyeable) {
                    output.applyComponents(ingredient.getComponentsPatch());
                }
            }
            for (int i = 0; i < inv.size(); i++) { // For each slot in the crafting inventory,
                final ItemStack ingredient = inv.getItem(i); // Get the ingredient in the slot
                DyeColor color = DyeColor.getColor(ingredient);
                if (!ingredient.isEmpty() && color != null) {
                    if (output.getItem() instanceof IDyeable dyeable) {
                        dyeable.onDye(output, color);
                    }
                }
            }
        }
        return output;
    }

    @Override
    @SuppressWarnings("unchecked")
    public RecipeSerializer<ShapelessRecipe> getSerializer() {
        return (RecipeSerializer<ShapelessRecipe>) (RecipeSerializer<?>) RecipeRegistry.DYE_RECIPE.get();
    }

}
