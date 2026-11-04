package com.misterd.agritechevolved.recipe;

import com.misterd.agritechevolved.AgritechEvolved;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ATERecipeTypes {

    public static final RecipeBookCategory CROP_RECIPE_BOOK_CATEGORY = new RecipeBookCategory();
    public static final RecipeBookCategory TREE_RECIPE_BOOK_CATEGORY = new RecipeBookCategory();

    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, AgritechEvolved.MODID);

    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, AgritechEvolved.MODID);

    public static final DeferredHolder<RecipeType<?>, RecipeType<CropRecipe>> CROP_TYPE =
            RECIPE_TYPES.register("crop", () -> new RecipeType<>() {
                @Override
                public String toString() { return AgritechEvolved.MODID + ":crop"; }
            });

    public static final DeferredHolder<RecipeType<?>, RecipeType<TreeRecipe>> TREE_TYPE =
            RECIPE_TYPES.register("tree", () -> new RecipeType<>() {
                @Override
                public String toString() { return AgritechEvolved.MODID + ":tree"; }
            });

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<CropRecipe>> CROP_SERIALIZER =
            RECIPE_SERIALIZERS.register("crop", () -> new RecipeSerializer<>(CropRecipe.CODEC, CropRecipe.STREAM_CODEC));

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<TreeRecipe>> TREE_SERIALIZER =
            RECIPE_SERIALIZERS.register("tree", () -> new RecipeSerializer<>(TreeRecipe.CODEC, TreeRecipe.STREAM_CODEC));

    public static void register(IEventBus eventBus) {
        RECIPE_TYPES.register(eventBus);
        RECIPE_SERIALIZERS.register(eventBus);
    }
}