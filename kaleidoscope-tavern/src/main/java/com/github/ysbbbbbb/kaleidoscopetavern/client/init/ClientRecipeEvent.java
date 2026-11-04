package com.github.ysbbbbbb.kaleidoscopetavern.client.init;

import com.github.ysbbbbbb.kaleidoscopetavern.KaleidoscopeTavern;
import com.github.ysbbbbbb.kaleidoscopetavern.crafting.recipe.BarrelRecipe;
import com.github.ysbbbbbb.kaleidoscopetavern.crafting.recipe.PressingTubRecipe;
import com.github.ysbbbbbb.kaleidoscopetavern.init.ModRecipes;
import com.google.common.collect.Lists;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMap;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RecipesReceivedEvent;

import java.util.Collections;
import java.util.List;

@EventBusSubscriber(value = Dist.CLIENT, modid = KaleidoscopeTavern.MOD_ID)
public class ClientRecipeEvent {
    public static List<RecipeHolder<BarrelRecipe>> BARREL_RECIPES = Collections.emptyList();
    public static List<RecipeHolder<PressingTubRecipe>> PRESSING_TUB_RECIPES = Collections.emptyList();

    @SubscribeEvent
    public static void onRecipeReceived(RecipesReceivedEvent event) {
        RecipeMap recipeMap = event.getRecipeMap();
        BARREL_RECIPES = Lists.newArrayList(recipeMap.byType(ModRecipes.BARREL_RECIPE.get()));
        PRESSING_TUB_RECIPES = Lists.newArrayList(recipeMap.byType(ModRecipes.PRESSING_TUB_RECIPE.get()));
    }
}
