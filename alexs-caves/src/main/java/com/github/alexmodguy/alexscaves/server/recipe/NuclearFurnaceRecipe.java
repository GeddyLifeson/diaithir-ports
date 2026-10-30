package com.github.alexmodguy.alexscaves.server.recipe;

import com.github.alexmodguy.alexscaves.server.block.ACBlockRegistry;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

public class NuclearFurnaceRecipe extends AbstractCookingRecipe {
    public static final MapCodec<NuclearFurnaceRecipe> MAP_CODEC = cookingMapCodec(NuclearFurnaceRecipe::new, 100);
    public static final StreamCodec<RegistryFriendlyByteBuf, NuclearFurnaceRecipe> STREAM_CODEC = cookingStreamCodec(NuclearFurnaceRecipe::new);
    public static final RecipeSerializer<NuclearFurnaceRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    public NuclearFurnaceRecipe(Recipe.CommonInfo commonInfo, AbstractCookingRecipe.CookingBookInfo bookInfo, Ingredient ingredient, ItemStackTemplate result, float experience, int cookingTime) {
        super(commonInfo, bookInfo, ingredient, result, experience, cookingTime);
    }

    @Override
    protected Item furnaceIcon() {
        return ACBlockRegistry.NUCLEAR_FURNACE.get().asItem();
    }

    @Override
    public RecipeSerializer<NuclearFurnaceRecipe> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public RecipeType<NuclearFurnaceRecipe> getType() {
        return ACRecipeRegistry.NUCLEAR_FURNACE_TYPE.get();
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.FURNACE_MISC;
    }
}
