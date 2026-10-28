package com.github.alexmodguy.alexscaves.server.recipe;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ACRecipeRegistry {
    public static final DeferredRegister<RecipeType<?>> TYPE_DEF_REG = DeferredRegister.create(Registries.RECIPE_TYPE, AlexsCaves.MODID);
    public static final DeferredRegister<RecipeSerializer<?>> DEF_REG = DeferredRegister.create(Registries.RECIPE_SERIALIZER, AlexsCaves.MODID);

    public static final DeferredHolder<RecipeType<?>, RecipeType<NuclearFurnaceRecipe>> NUCLEAR_FURNACE_TYPE = TYPE_DEF_REG.register("nuclear_furnace", () -> RecipeType.simple(Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "nuclear_furnace")));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<NuclearFurnaceRecipe>> NUCLEAR_FURNACE = DEF_REG.register("nuclear_furnace", () -> NuclearFurnaceRecipe.SERIALIZER);
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<RecipeCaveMap>> CAVE_MAP = DEF_REG.register("cave_map", () -> RecipeCaveMap.SERIALIZER);
}
