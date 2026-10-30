package com.github.ysbbbbbb.kaleidoscopetavern.init;

import com.github.ysbbbbbb.kaleidoscopetavern.KaleidoscopeTavern;
import com.github.ysbbbbbb.kaleidoscopetavern.crafting.recipe.BarrelRecipe;
import com.github.ysbbbbbb.kaleidoscopetavern.crafting.recipe.PressingTubRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public interface ModRecipes {
    DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, KaleidoscopeTavern.MOD_ID);
    DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, KaleidoscopeTavern.MOD_ID);
    DeferredRegister<RecipeBookCategory> RECIPE_BOOK_CATEGORIES = DeferredRegister.create(Registries.RECIPE_BOOK_CATEGORY, KaleidoscopeTavern.MOD_ID);

    Supplier<RecipeSerializer<PressingTubRecipe>> PRESSING_TUB_SERIALIZER = RECIPE_SERIALIZERS.register("pressing_tub", () -> PressingTubRecipe.SERIALIZER);
    Supplier<RecipeSerializer<BarrelRecipe>> BARREL_SERIALIZER = RECIPE_SERIALIZERS.register("barrel", () -> BarrelRecipe.SERIALIZER);

    Supplier<RecipeType<PressingTubRecipe>> PRESSING_TUB_RECIPE = RECIPE_TYPES.register("pressing_tub", () -> RecipeType.simple(KaleidoscopeTavern.modLoc("pressing_tub")));
    Supplier<RecipeType<BarrelRecipe>> BARREL_RECIPE = RECIPE_TYPES.register("barrel", () -> RecipeType.simple(KaleidoscopeTavern.modLoc("barrel")));

    Supplier<RecipeBookCategory> PRESSING_TUB_RECIPE_CATEGORY = RECIPE_BOOK_CATEGORIES.register("pressing_tub", RecipeBookCategory::new);
    Supplier<RecipeBookCategory> BARREL_RECIPE_CATEGORY = RECIPE_BOOK_CATEGORIES.register("barrel", RecipeBookCategory::new);
}
