/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.api.recipe;

import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;

import vazkii.botania.common.lib.LibBlockNames;

import java.util.Objects;

import static vazkii.botania.api.BotaniaAPI.botaniaRL;

public interface TerrestrialAgglomerationRecipe extends Recipe<ProcessingRecipeInput> {
	Identifier TERRA_PLATE_ID = botaniaRL(LibBlockNames.TERRESTRIAL_AGGLOMERATION_PLATE);
	Identifier TYPE_ID = TERRA_PLATE_ID;

	int getMana();

	@Override
	@SuppressWarnings("unchecked")
	default RecipeType<? extends Recipe<ProcessingRecipeInput>> getType() {
		return (RecipeType<? extends Recipe<ProcessingRecipeInput>>) Objects.requireNonNull(BuiltInRegistries.RECIPE_TYPE.getValue(TYPE_ID));
	}

	default NonNullList<ItemStack> getRemainingItems(ProcessingRecipeInput input) {
		// terra plate always consumes all input items
		return NonNullList.create();
	}

	default ItemStack getToastSymbol() {
		return BuiltInRegistries.ITEM.getOptional(TERRA_PLATE_ID).map(ItemStack::new).orElse(ItemStack.EMPTY);
	}
}
