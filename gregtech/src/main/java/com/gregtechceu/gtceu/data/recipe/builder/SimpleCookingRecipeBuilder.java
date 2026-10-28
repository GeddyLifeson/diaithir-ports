package com.gregtechceu.gtceu.data.recipe.builder;

import com.gregtechceu.gtceu.api.recipe.ingredient.ExDataComponentIngredient;

import net.minecraft.core.component.predicates.DataComponentPredicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;

import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

@Accessors(chain = true, fluent = true)
public class SimpleCookingRecipeBuilder<T extends AbstractCookingRecipe> {

    private final RecipeConstructor<T> constructor;
    protected final String folder;

    protected Ingredient input;
    @Setter
    protected String group;
    @Setter
    protected CookingBookCategory category = CookingBookCategory.MISC;

    protected ItemStack output = ItemStack.EMPTY;
    @Setter
    protected float experience;
    @Setter
    protected int cookingTime;
    @Setter
    protected Identifier id;

    protected SimpleCookingRecipeBuilder(@Nullable Identifier id, String folder,
                                         RecipeConstructor<T> constructor) {
        this.id = id;
        this.folder = folder;
        this.constructor = constructor;
    }

    public static SimpleCookingRecipeBuilder<CampfireCookingRecipe> campfireCooking(@Nullable Identifier id) { return null; /* ported stub */ }

    public static SimpleCookingRecipeBuilder<SmeltingRecipe> smelting(@Nullable Identifier id) { return null; /* ported stub */ }

    public static SimpleCookingRecipeBuilder<BlastingRecipe> blasting(@Nullable Identifier id) { return null; /* ported stub */ }

    public static SimpleCookingRecipeBuilder<SmokingRecipe> smoking(@Nullable Identifier id) { return null; /* ported stub */ }

    public SimpleCookingRecipeBuilder<T> input(TagKey<Item> tag) { return null; /* ported stub */ }

    public SimpleCookingRecipeBuilder<T> input(TagKey<Item> tag, DataComponentPredicate components, boolean strict) {
        return input(ExDataComponentIngredient.of(strict, components, tag));
    }

    public SimpleCookingRecipeBuilder<T> input(ItemStack itemStack) { return null; /* ported stub */ }

    public SimpleCookingRecipeBuilder<T> input(ItemLike itemLike) {
        return input(Ingredient.of(itemLike));
    }

    public SimpleCookingRecipeBuilder<T> input(Ingredient ingredient) {
        input = ingredient;
        return this;
    }

    public SimpleCookingRecipeBuilder<T> output(ItemStack itemStack) {
        this.output = itemStack.copy();
        return this;
    }

    public SimpleCookingRecipeBuilder<T> output(ItemStack itemStack, int count) {
        this.output = itemStack.copyWithCount(count);
        return this;
    }

    protected Identifier defaultId() {
        return BuiltInRegistries.ITEM.getKey(output.getItem());
    }

    private T create() {
        return constructor.create(Objects.requireNonNullElse(this.group, ""), this.category, this.input, this.output,
                this.experience, this.cookingTime);
    }

    public void save(RecipeOutput consumer) {  /* ported stub */ }

    @FunctionalInterface
    public interface RecipeConstructor<T extends AbstractCookingRecipe> {

        T create(String group, CookingBookCategory category,
                 Ingredient ingredient, ItemStack result, float experience, int cookingTime);
    }
}
