package com.gregtechceu.gtceu.data.pack;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.lookup.StagingRecipeDB;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeMap;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Runs after the vanilla recipe manager: removes recipes GTCEu replaces and fills each GT recipe type's lookup DB.
 * Replaces RecipeManagerEarlyMixin / RecipeManagerLateMixin.
 */
public class GTRecipePostProcessor implements PreparableReloadListener {

    private final RecipeManager manager;

    public GTRecipePostProcessor(RecipeManager manager) {
        this.manager = manager;
    }

    @Override
    public CompletableFuture<Void> reload(SharedState sharedState, Executor backgroundExecutor,
                                          PreparationBarrier stage, Executor gameExecutor) {
        return stage.wait(null).thenRunAsync(this::apply, gameExecutor);
    }

    /** Fills the lookup DBs. Needs bound tags, so it runs on TagsUpdatedEvent, not during the reload. */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static void fillDatabases(RecipeManager manager) {
        try {
            RecipeMap map = manager.recipes;
            int total = 0;
            for (var type : BuiltInRegistries.RECIPE_TYPE) {
                if (type instanceof GTRecipeType gtType) {
                    var staging = new StagingRecipeDB();
                    for (Object o : map.byType((net.minecraft.world.item.crafting.RecipeType) gtType)) {
                        RecipeHolder<?> holder = (RecipeHolder<?>) o;
                        if (holder.value() instanceof GTRecipe recipe) {
                            recipe.setId(holder.id().identifier());
                            staging.add(recipe);
                            total++;
                        }
                    }
                    staging.populateDB(gtType.db());
                }
            }
            GTCEu.LOGGER.info("Filled GT recipe lookup with {} recipes", total);
        } catch (Throwable t) {
            GTCEu.LOGGER.error("GT recipe DB fill failed", t);
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void apply() {
        try {
            var removals = GTDynamicDataGenerator.REMOVALS;
            RecipeMap map = manager.recipes;
            if (!removals.isEmpty()) {
                var kept = new ArrayList<RecipeHolder<?>>();
                for (var holder : map.values()) {
                    if (!removals.contains(holder.id().identifier())) kept.add(holder);
                }
                map = RecipeMap.create(kept);
                manager.recipes = map;
                GTCEu.LOGGER.info("Removed {} recipes replaced by GTCEu", removals.size());
            }
        } catch (Throwable t) {
            GTCEu.LOGGER.error("GT recipe post processing failed", t);
        }
    }
}
