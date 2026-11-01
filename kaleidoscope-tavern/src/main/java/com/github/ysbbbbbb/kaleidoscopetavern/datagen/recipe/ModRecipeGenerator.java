package com.github.ysbbbbbb.kaleidoscopetavern.datagen.recipe;

import com.google.common.collect.Lists;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ModRecipeGenerator extends ModRecipeProvider {
    private final List<ModRecipeProvider> providers = Lists.newArrayList();

    public ModRecipeGenerator(HolderLookup.Provider registries, RecipeOutput output) {
        super(registries, output);
        providers.add(new PressingTubRecipeProvider(registries, output));
        providers.add(new BarrelRecipeProvider(registries, output));
        providers.add(new ShapedRecipeProvider(registries, output));
        providers.add(new ShapelessRecipeProvider(registries, output));
    }

    @Override
    protected void buildRecipes() {
        for (ModRecipeProvider provider : providers) {
            provider.buildRecipes();
        }
    }

    @SuppressWarnings("all")
    public static class Runner extends RecipeProvider.Runner {
        public Runner(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> registries) {
            super(packOutput, registries);
        }

        @Override
        protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
            return new ModRecipeGenerator(registries, output);
        }

        @Override
        public String getName() {
            return "kaleidoscope Tavern Recipes";
        }
    }
}
