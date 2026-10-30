package com.github.ysbbbbbb.kaleidoscopetavern.datagen.builder;

import com.github.ysbbbbbb.kaleidoscopetavern.KaleidoscopeTavern;
import com.github.ysbbbbbb.kaleidoscopetavern.crafting.recipe.BarrelRecipe;
import com.github.ysbbbbbb.kaleidoscopetavern.crafting.serializer.BarrelRecipeSerializer;
import com.github.ysbbbbbb.kaleidoscopetavern.init.ModItems;
import com.google.common.collect.Lists;
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

import java.util.List;

public class BarrelBuilder implements RecipeBuilder {
    private static final String NAME = "barrel";

    private final List<Ingredient> ingredients = Lists.newArrayList();

    private Fluid fluid = Fluids.WATER;
    private Ingredient carrier = Ingredient.of(ModItems.EMPTY_BOTTLE.get());
    private ItemStackTemplate result = new ItemStackTemplate(Items.GRASS_BLOCK);
    private int unitTime = BarrelRecipeSerializer.DEFAULT_UNIT_TIME;

    public static BarrelBuilder builder() {
        return new BarrelBuilder();
    }

    public BarrelBuilder addIngredient(ItemLike itemLike) {
        this.ingredients.add(Ingredient.of(itemLike));
        return this;
    }

    public BarrelBuilder addIngredient(HolderSet<Item> holders) {
        this.ingredients.add(Ingredient.of(holders));
        return this;
    }

    public BarrelBuilder addIngredient(Ingredient ingredient) {
        this.ingredients.add(ingredient);
        return this;
    }

    public BarrelBuilder setFluid(Fluid fluid) {
        this.fluid = fluid;
        return this;
    }

    public BarrelBuilder setCarrier(ItemLike itemLike) {
        this.carrier = Ingredient.of(itemLike);
        return this;
    }

    public BarrelBuilder setCarrier(HolderSet<Item> holders) {
        this.carrier = Ingredient.of(holders);
        return this;
    }

    public BarrelBuilder setResult(ItemLike itemLike) {
        this.result = new ItemStackTemplate(itemLike.asItem());
        return this;
    }

    public BarrelBuilder setUnitTime(int unitTime) {
        this.unitTime = unitTime;
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
        String path = RecipeBuilder.getDefaultRecipeId(this.result).identifier().getPath();
        Identifier filePath = KaleidoscopeTavern.modLoc(NAME + "/" + path);
        return ResourceKey.create(Registries.RECIPE, filePath);
    }

    @Override
    public void save(RecipeOutput output, ResourceKey<Recipe<?>> key) {
        BarrelRecipe recipe = new BarrelRecipe(List.copyOf(this.ingredients), fluid, carrier, result, unitTime);
        output.accept(key, recipe, null);
    }
}
