package vectorwing.farmersdelight.common.utility;

import vectorwing.farmersdelight.common.crafting.CookingPotRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import vectorwing.farmersdelight.FarmersDelight;

public class RecipeUtils
{
	// Copyright (c) 2014-2015 mezz
	public static ItemStack getResultItem(Recipe<?> recipe) {
		// recipes no longer expose a result in general (1.21.2); only the cooking pot recipe is shown through here
		return recipe instanceof CookingPotRecipe cooking ? cooking.getResultItem() : ItemStack.EMPTY;
	}

	public static Identifier FDLocation(String name) {
		return Identifier.fromNamespaceAndPath(FarmersDelight.MODID, name);
	}

	public static ResourceKey<Recipe<?>> FDKey(String path) {
		return ResourceKey.create(Registries.RECIPE, RecipeUtils.FDLocation(path));
	}
}
