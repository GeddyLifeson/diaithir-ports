package dev.shadowsoffire.apothic_enchanting.table.infusion;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

import dev.shadowsoffire.apothic_enchanting.Ench;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMap;

/**
 * Cached list of {@link InfusionRecipe} instances, refreshed whenever the server finishes a reload
 * or the client receives the recipe map from the server.
 *
 * <p>The old 1.21.1 codepath used {@code Level#getRecipeManager().getAllRecipesFor(type)}. That method
 * is gone in 26.1 — {@link net.minecraft.world.item.crafting.RecipeAccess} only exposes property sets
 * and stonecutter recipes. Instead, we subscribe to the NeoForge reload hooks and cache the recipes
 * via {@link RecipeMap#byType(net.minecraft.world.item.crafting.RecipeType)}.
 */
public final class InfusionRecipeCache {

    private static volatile List<InfusionRecipe> RECIPES = List.of();

    private InfusionRecipeCache() {}

    /** Called from {@code ApothEnchEvents} on {@code ServerStartedEvent} / {@code OnDatapackSyncEvent}. */
    public static void rebuildFromServer(MinecraftServer server) {
        rebuildFromMap(server.getRecipeManager().recipeMap());
    }

    /** Called from {@code ApothEnchClient} on {@code RecipesReceivedEvent}. */
    public static void rebuildFromMap(RecipeMap map) {
        Collection<RecipeHolder<InfusionRecipe>> holders = map.byType(Ench.RecipeTypes.INFUSION);
        RECIPES = holders.stream().map(RecipeHolder::value).toList();
    }

    public static void clear() {
        RECIPES = List.of();
    }

    public static List<InfusionRecipe> all() {
        return RECIPES;
    }

    @Nullable
    public static InfusionRecipe findMatch(ItemStack input, float eterna, float quanta, float arcana) {
        return RECIPES.stream()
            .sorted((r1, r2) -> -Float.compare(r1.getRequirements().eterna(), r2.getRequirements().eterna()))
            .filter(r -> r.matches(input, eterna, quanta, arcana))
            .findFirst().orElse(null);
    }

    @Nullable
    public static InfusionRecipe findItemMatch(ItemStack toEnchant) {
        return RECIPES.stream().filter(r -> r.getInput().test(toEnchant)).findFirst().orElse(null);
    }
}
