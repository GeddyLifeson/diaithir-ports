package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.recipe.*;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;

import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class GTRecipeSerializers {

    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister
            .create(Registries.RECIPE_SERIALIZER, GTCEu.MOD_ID);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<StrictShapedRecipe>> CRAFTING_SHAPED_STRICT = RECIPE_SERIALIZERS
            .register("crafting_shaped_strict", () -> StrictShapedRecipe.SERIALIZER_STRICT);
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ShapedEnergyTransferRecipe>> CRAFTING_SHAPED_ENERGY_TRANSFER = RECIPE_SERIALIZERS
            .register("crafting_shaped_energy_transfer", () -> ShapedEnergyTransferRecipe.SERIALIZER_ENERGY);
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ShapedFluidContainerRecipe>> CRAFTING_SHAPED_FLUID_CONTAINER = RECIPE_SERIALIZERS
            .register("crafting_shaped_fluid_container", () -> ShapedFluidContainerRecipe.SERIALIZER_FLUID);
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<GTRecipe>> GT_RECIPE = RECIPE_SERIALIZERS
            .register("gt_recipe", () -> GTRecipeSerializer.SERIALIZER);
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<FacadeCoverRecipe>> FACADE_COVER = RECIPE_SERIALIZERS
            .register("facade_cover", () -> FacadeCoverRecipe.SERIALIZER);
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ToolHeadReplaceRecipe>> TOOL_HEAD_REPLACE = RECIPE_SERIALIZERS
            .register("tool_head_replace", () -> ToolHeadReplaceRecipe.SERIALIZER);
}
