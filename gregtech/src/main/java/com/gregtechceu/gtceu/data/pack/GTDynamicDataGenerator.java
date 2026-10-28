package com.gregtechceu.gtceu.data.pack;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.data.GTRecipes;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;

import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;

/** Fills {@link GTDynamicDataPack} with everything the 1.21.1 mod generated at reload time. */
public final class GTDynamicDataGenerator {

    private GTDynamicDataGenerator() {}

    public static HolderLookup.Provider registries() {
        return RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
    }

    public static void addJson(String folder, Identifier id, JsonElement json) {
        GTDynamicDataPack.addResource(id.withPath(folder + "/" + id.getPath() + ".json"),
                json.toString().getBytes(StandardCharsets.UTF_8));
    }

    public static void generate() {
        var registries = registries();
        var ops = registries.createSerializationContext(JsonOps.INSTANCE);
        int[] count = new int[2];
        RecipeOutput output = new RecipeOutput() {

            @Override
            public void accept(ResourceKey<Recipe<?>> id, Recipe<?> recipe, @Nullable AdvancementHolder advancement) {
                try {
                    JsonElement json = Recipe.CODEC.encodeStart(ops, recipe).getOrThrow();
                    addJson("recipe", id.identifier(), json);
                    count[0]++;
                } catch (Throwable t) {
                    count[1]++;
                    if (count[1] <= 20) GTCEu.LOGGER.error("Could not encode recipe {}: {}", id.identifier(),
                            t.toString());
                }
            }

            @SuppressWarnings("removal")
            @Override
            public Advancement.Builder advancement() {
                return Advancement.Builder.recipeAdvancement();
            }

            @Override
            public void includeRootAdvancement() {}
        };
        GTRecipes.recipeAddition(output);
        GTCEu.LOGGER.info("Encoded {} GT recipes ({} failed)", count[0], count[1]);
    }
}
