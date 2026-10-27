package com.diaithir.addons.newage;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import com.diaithir.addons.newage.content.energising.recipe.EnergisingRecipe;

public class CNARecipeTypes {
    private static final DeferredRegister<RecipeType<?>> TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, CreateNewAge.MOD_ID);
    private static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, CreateNewAge.MOD_ID);

    public static final DeferredHolder<RecipeType<?>, RecipeType<EnergisingRecipe>> ENERGISING =
            TYPES.register("energising", () -> RecipeType.simple(Identifier.fromNamespaceAndPath(CreateNewAge.MOD_ID, "energising")));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<EnergisingRecipe>> ENERGISING_SERIALIZER =
            SERIALIZERS.register("energising", () -> EnergisingRecipe.SERIALIZER);

    public static void register(IEventBus bus) {
        TYPES.register(bus);
        SERIALIZERS.register(bus);
    }
}
