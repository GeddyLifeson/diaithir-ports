/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.api.recipe;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.Objects;

import static vazkii.botania.api.BotaniaAPI.botaniaRL;

public interface PetalApothecaryRecipe extends RecipeWithReagent<ProcessingRecipeInput> {
	Identifier TYPE_ID = botaniaRL("petal_apothecary");

	@Override
	@SuppressWarnings("unchecked")
	default RecipeType<? extends Recipe<ProcessingRecipeInput>> getType() {
		return (RecipeType<? extends Recipe<ProcessingRecipeInput>>) Objects.requireNonNull(BuiltInRegistries.RECIPE_TYPE.getValue(TYPE_ID));
	}
}
