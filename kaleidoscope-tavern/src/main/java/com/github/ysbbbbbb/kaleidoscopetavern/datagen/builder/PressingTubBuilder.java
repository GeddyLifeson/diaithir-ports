package com.github.ysbbbbbb.kaleidoscopetavern.datagen.builder;

import com.github.ysbbbbbb.kaleidoscopetavern.KaleidoscopeTavern;
import com.github.ysbbbbbb.kaleidoscopetavern.crafting.recipe.PressingTubRecipe;
import com.github.ysbbbbbb.kaleidoscopetavern.crafting.serializer.PressingTubRecipeSerializer;
import net.minecraft.advancements.Criterion;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;

public class PressingTubBuilder implements RecipeBuilder {
    private static final String NAME = "pressing_tub";

    private Ingredient ingredient = Ingredient.of(Items.GRASS_BLOCK);
    private Fluid fluid = Fluids.WATER;
    private int fluidAmount = PressingTubRecipeSerializer.DEFAULT_FLUID_AMOUNT;

    public static PressingTubBuilder builder() {
        return new PressingTubBuilder();
    }

    public PressingTubBuilder setIngredient(ItemLike itemLike) {
        this.ingredient = Ingredient.of(itemLike);
        return this;
    }

    public PressingTubBuilder setIngredient(HolderSet<Item> holders) {
        this.ingredient = Ingredient.of(holders);
        return this;
    }

    public PressingTubBuilder setFluid(Fluid fluid) {
        this.fluid = fluid;
        return this;
    }

    public PressingTubBuilder setFluidAmount(int amount) {
        this.fluidAmount = amount;
        return this;
    }

    @Override
    public RecipeBuilder unlockedBy(String name, Criterion<?> trigger) {
        return this;
    }

    @Override
    public RecipeBuilder group(@Nullable String groupName) {
        return this;
    }

    @Override
    public ResourceKey<Recipe<?>> defaultId() {
        ItemStackTemplate stack = new ItemStackTemplate(this.fluid.getBucket());
        String path = RecipeBuilder.getDefaultRecipeId(stack).identifier().getPath();
        Identifier filePath = KaleidoscopeTavern.modLoc(NAME + "/" + path);
        return ResourceKey.create(Registries.RECIPE, filePath);
    }

    @Override
    public void save(RecipeOutput output, ResourceKey<Recipe<?>> key) {
        PressingTubRecipe recipe = new PressingTubRecipe(this.ingredient, this.fluid, this.fluidAmount);
        output.accept(key, recipe, null);
    }
}
