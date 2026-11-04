package net.diaithir.druidry.ars.common.crafting.recipes;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public interface IEnchantingRecipe extends Recipe<ApparatusRecipeInput> {

    @Override
    default boolean matches(ApparatusRecipeInput input, Level level) {
        return matches(input, level, null);
    }

    boolean matches(ApparatusRecipeInput input, Level level, @Nullable Player player);

    default boolean consumesSource() {
        return sourceCost() > 0;
    }

    int sourceCost();

    @Override
    default boolean isSpecial() {
        return true;
    }

        default boolean canCraftInDimensions(int pWidth, int pHeight) {
        return false;
    }

    @Override
    default boolean showNotification() {
        return false;
    }

    @Override
    default String group() {
        return "";
    }

    @Override
    default net.minecraft.world.item.crafting.PlacementInfo placementInfo() {
        return net.minecraft.world.item.crafting.PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    default net.minecraft.world.item.crafting.RecipeBookCategory recipeBookCategory() {
        return net.minecraft.world.item.crafting.RecipeBookCategories.CRAFTING_MISC;
    }
}
