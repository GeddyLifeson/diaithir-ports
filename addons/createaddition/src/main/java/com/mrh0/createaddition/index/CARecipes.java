package com.mrh0.createaddition.index;

import com.mrh0.createaddition.CreateAddition;
import com.mrh0.createaddition.recipe.charging.ChargingRecipe;
import com.mrh0.createaddition.recipe.rolling.RollingRecipe;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

public class CARecipes {
    public static final RecipeType<RollingRecipe> ROLLING_TYPE = type("rolling");
    public static final RecipeType<ChargingRecipe> CHARGING_TYPE = type("charging");
    public static final RecipeSerializer<RollingRecipe> ROLLING = serializer("rolling", RollingRecipe.SERIALIZER);
    public static final RecipeSerializer<ChargingRecipe> CHARGING = serializer("charging", ChargingRecipe.SERIALIZER);

    private static <T extends Recipe<?>> RecipeType<T> type(String name) {
        Identifier id = CreateAddition.asResource(name);
        return Registry.register(BuiltInRegistries.RECIPE_TYPE, id, new RecipeType<T>() {
            @Override public String toString() { return id.toString(); }
        });
    }

    private static <T extends Recipe<?>> RecipeSerializer<T> serializer(String name, RecipeSerializer<T> s) {
        return Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, CreateAddition.asResource(name), s);
    }

    public static void register() {}
}
