package net.diaithir.druidry.ars.common.crafting.recipes;

import net.diaithir.druidry.ars.common.items.SpellBook;
import net.diaithir.druidry.ars.setup.registry.RecipeRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStackTemplate;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import org.jetbrains.annotations.NotNull;

public class BookUpgradeRecipe extends ShapelessRecipe {

    public BookUpgradeRecipe(Recipe.CommonInfo commonInfo, CraftingRecipe.CraftingBookInfo bookInfo, ItemStackTemplate result, List<Ingredient> ingredients) {
        super(commonInfo, bookInfo, result, ingredients);
    }

    @Override
    public @NotNull ItemStack assemble(final @NotNull CraftingInput inv) {
        final ItemStack output = super.assemble(inv);

        if (!output.isEmpty()) {
            for (int i = 0; i < inv.size(); i++) { // For each slot in the crafting inventory,
                final ItemStack ingredient = inv.getItem(i); // Get the ingredient in the slot
                if (!ingredient.isEmpty() && ingredient.getItem() instanceof SpellBook) {
                    output.applyComponents(ingredient.getComponentsPatch());
                }
            }
        }

        return output; // Return the modified output
    }

    @Override
    @SuppressWarnings("unchecked")
    public RecipeSerializer<ShapelessRecipe> getSerializer() {
        return (RecipeSerializer<ShapelessRecipe>) (RecipeSerializer<?>) RecipeRegistry.BOOK_UPGRADE_RECIPE.get();
    }
}
