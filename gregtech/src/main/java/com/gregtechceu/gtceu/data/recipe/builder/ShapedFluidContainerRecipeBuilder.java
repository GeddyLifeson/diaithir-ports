package com.gregtechceu.gtceu.data.recipe.builder;

import com.gregtechceu.gtceu.api.recipe.ShapedFluidContainerRecipe;

import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class ShapedFluidContainerRecipeBuilder extends ShapedRecipeBuilder {

    public ShapedFluidContainerRecipeBuilder(@Nullable Identifier id) {
        super(id);
    }

    public void save(RecipeOutput consumer) {
        var recipeId = id == null ? defaultId() : id;
        ShapedRecipe recipe = new ShapedFluidContainerRecipe(new net.minecraft.world.item.crafting.Recipe.CommonInfo(false), new net.minecraft.world.item.crafting.CraftingRecipe.CraftingBookInfo(net.minecraft.data.recipes.RecipeBuilder.determineCraftingBookCategory(this.category), Objects.requireNonNullElse(this.group, "")), ShapedRecipePattern.of(key, rows), net.minecraft.world.item.ItemStackTemplate.fromNonEmptyStack(this.output));
        consumer.accept(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.RECIPE, recipeId.withPrefix("shaped/")), recipe, null);
    }
}
