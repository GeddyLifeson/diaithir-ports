package io.github.tr100000.researcher.impl.recipe;

import io.github.tr100000.researcher.api.recipe.RecipeUnlockDisplay;
import io.github.tr100000.trutils.api.gui.ItemIcon;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.item.crafting.TransmuteRecipe;
import net.minecraft.world.item.crafting.display.RecipeDisplay;

public final class CraftingRecipeUnlockDisplay {
    private CraftingRecipeUnlockDisplay() {}

    public static RecipeUnlockDisplay createShaped(RecipeHolder<ShapedRecipe> entry) {
        return createNormal(entry.value(), entry.id().identifier());
    }

    public static RecipeUnlockDisplay createShapeless(RecipeHolder<ShapelessRecipe> entry) {
        return createNormal(entry.value(), entry.id().identifier());
    }

    private static RecipeUnlockDisplay createNormal(CraftingRecipe recipe, Identifier recipeId) {
        if (recipe.display().isEmpty()) return RecipeUnlockDisplay.ERROR;
        if (recipe.placementInfo().isImpossibleToPlace()) return RecipeUnlockDisplay.ERROR;

        ContextMap contextParameterMap = RecipeUnlockDisplay.contextParameterMap();
        RecipeDisplay recipeDisplay = recipe.display().getFirst();
        CraftingRecipeTooltipComponent tooltip = CraftingRecipeTooltipComponent.create(
                recipeId,
                recipe instanceof ShapedRecipe shaped ? shaped.getWidth() : 3,
                recipe.placementInfo(),
                recipeDisplay.result(),
                contextParameterMap
        );
        ItemStack resultStack = recipeDisplay.result().resolveForFirstStack(contextParameterMap);
        return RecipeUnlockDisplay.of(new ItemIcon(ItemStackTemplate.fromNonEmptyStack(resultStack)), tooltip);
    }

    public static RecipeUnlockDisplay createTransmute(RecipeHolder<TransmuteRecipe> entry) {
        TransmuteRecipe recipe = entry.value();
        if (recipe.display().isEmpty()) return RecipeUnlockDisplay.ERROR;
        if (recipe.placementInfo().isImpossibleToPlace()) return RecipeUnlockDisplay.ERROR;

        ContextMap contextParameterMap = RecipeUnlockDisplay.contextParameterMap();
        RecipeDisplay recipeDisplay = recipe.display().getFirst();
        CraftingRecipeTooltipComponent tooltip = CraftingRecipeTooltipComponent.create(
                entry.id().identifier(),
                2,
                recipe.placementInfo(),
                recipeDisplay.result(),
                contextParameterMap
        );
        ItemStack resultStack = recipeDisplay.result().resolveForFirstStack(contextParameterMap);
        return RecipeUnlockDisplay.of(new ItemIcon(ItemStackTemplate.fromNonEmptyStack(resultStack)), tooltip);
    }
}
