package io.github.tr100000.researcher;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMap;
import org.jspecify.annotations.Nullable;

/** Crafting recipes received from the server (the server asks for them in a datapack sync). */
public final class ClientSyncedRecipes {
    private ClientSyncedRecipes() {}

    private static RecipeMap recipes = RecipeMap.EMPTY;

    public static void set(RecipeMap map) {
        recipes = map;
    }

    public static void clear() {
        recipes = RecipeMap.EMPTY;
    }

    public static @Nullable RecipeHolder<?> get(ResourceKey<Recipe<?>> key) {
        return recipes.byKey(key);
    }
}
