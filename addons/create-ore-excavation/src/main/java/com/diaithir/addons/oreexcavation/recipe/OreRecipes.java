package com.diaithir.addons.oreexcavation.recipe;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;

import com.diaithir.addons.oreexcavation.CreateOreExcavation;

/** Side-agnostic recipe access. The client map is filled from RecipesReceivedEvent. */
public class OreRecipes {
	public static RecipeMap clientMap = RecipeMap.EMPTY;

	public static RecipeMap map(Level level) {
		if (level instanceof ServerLevel sl)
			return sl.getServer().getRecipeManager().recipeMap();
		return clientMap;
	}

	public static <T extends Recipe<?>> Collection<RecipeHolder<T>> all(Level level, RecipeType<T> type) {
		return (Collection) map(level).byType((RecipeType) type);
	}

	public static ResourceKey<Recipe<?>> key(Identifier id) {
		return ResourceKey.create(Registries.RECIPE, id);
	}

	public static RecipeHolder<?> byId(Level level, Identifier id) {
		return id == null ? null : map(level).byKey(key(id));
	}

	@SuppressWarnings("unchecked")
	public static RecipeHolder<VeinRecipe> vein(Level level, Identifier id) {
		RecipeHolder<?> h = byId(level, id);
		return h != null && h.value() instanceof VeinRecipe ? (RecipeHolder<VeinRecipe>) h : null;
	}

	public static Collection<RecipeHolder<VeinRecipe>> veins(Level level) {
		return all(level, CreateOreExcavation.VEIN_RECIPES.getRecipeType());
	}

	public static Identifier idOf(RecipeHolder<?> h) {
		return h.id().identifier();
	}

	public static List<Identifier> veinIds(Level level) {
		return veins(level).stream().map(OreRecipes::idOf).collect(Collectors.toList());
	}

}
