package vectorwing.farmersdelight.common.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredRegister;
import vectorwing.farmersdelight.FarmersDelight;
import vectorwing.farmersdelight.common.crafting.CookingPotRecipe;
import vectorwing.farmersdelight.common.crafting.CuttingBoardRecipe;

import java.util.function.Supplier;

public class ModRecipeTypes
{
	public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, FarmersDelight.MODID);

	public static final Supplier<RecipeType<CookingPotRecipe>> COOKING = RECIPE_TYPES.register("cooking", () -> registerRecipeType("cooking"));
	public static final DeferredRegister<RecipeBookCategory> RECIPE_BOOK_CATEGORIES = DeferredRegister.create(Registries.RECIPE_BOOK_CATEGORY, FarmersDelight.MODID);
	public static final Supplier<RecipeBookCategory> COOKING_BOOK_CATEGORY = RECIPE_BOOK_CATEGORIES.register("cooking", RecipeBookCategory::new);
	public static final Supplier<RecipeBookCategory> CUTTING_BOOK_CATEGORY = RECIPE_BOOK_CATEGORIES.register("cutting", RecipeBookCategory::new);
	public static final Supplier<RecipeType<CuttingBoardRecipe>> CUTTING = RECIPE_TYPES.register("cutting", () -> registerRecipeType("cutting"));

	public static <T extends Recipe<?>> RecipeType<T> registerRecipeType(final String identifier) {
		return new RecipeType<>()
		{
			public String toString() {
				return FarmersDelight.MODID + ":" + identifier;
			}
		};
	}
}
