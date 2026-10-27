package org.dimdev.dimdoors.recipe;

import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.NotNull;

public interface TesselatingRecipe extends Recipe<CraftingInput> {

    int weavingTime();

    @Override
    default @NotNull RecipeType<TesselatingRecipe> getType() {
        return ModRecipeTypes.TESSELATING;
    }

    @Override
    default @NotNull RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }
}
