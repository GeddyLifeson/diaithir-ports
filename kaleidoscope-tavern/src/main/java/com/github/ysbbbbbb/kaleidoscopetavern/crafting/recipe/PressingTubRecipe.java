package com.github.ysbbbbbb.kaleidoscopetavern.crafting.recipe;

import com.github.ysbbbbbb.kaleidoscopetavern.crafting.serializer.PressingTubRecipeSerializer;
import com.github.ysbbbbbb.kaleidoscopetavern.init.ModRecipes;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;

public class PressingTubRecipe extends SingleItemRecipe {
    public static final RecipeSerializer<PressingTubRecipe> SERIALIZER = new RecipeSerializer<>(PressingTubRecipeSerializer.CODEC, PressingTubRecipeSerializer.STREAM_CODEC);
    private static final CommonInfo EMPTY = new CommonInfo(false);
    private final Fluid fluid;
    private final int fluidAmount;

    public PressingTubRecipe(Ingredient ingredient, Fluid fluid, int fluidAmount) {
        super(EMPTY, ingredient, new ItemStackTemplate(fluid.getBucket()));
        this.fluid = fluid;
        this.fluidAmount = fluidAmount;
    }

    @Override
    public RecipeSerializer<? extends SingleItemRecipe> getSerializer() {
        return ModRecipes.PRESSING_TUB_SERIALIZER.get();
    }

    @Override
    public RecipeType<? extends SingleItemRecipe> getType() {
        return ModRecipes.PRESSING_TUB_RECIPE.get();
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return ModRecipes.PRESSING_TUB_RECIPE_CATEGORY.get();
    }

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        return this.input().test(input.getItem(0));
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public String group() {
        return "";
    }

    public Fluid getFluid() {
        return fluid;
    }

    public int getFluidAmount() {
        return fluidAmount;
    }
}
