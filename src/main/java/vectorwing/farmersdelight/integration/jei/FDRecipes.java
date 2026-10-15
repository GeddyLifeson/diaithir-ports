package vectorwing.farmersdelight.integration.jei;

import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.ItemStackTemplate;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RecipesReceivedEvent;
import vectorwing.farmersdelight.FarmersDelight;
import vectorwing.farmersdelight.common.crafting.CookingPotRecipe;
import vectorwing.farmersdelight.common.crafting.CuttingBoardRecipe;
import vectorwing.farmersdelight.common.registry.ModItems;
import vectorwing.farmersdelight.common.registry.ModRecipeTypes;
import vectorwing.farmersdelight.common.utility.RecipeUtils;

import java.util.List;

/** FD's recipes as the client last received them; CommonEvents asks the server to send both types. */
@EventBusSubscriber(modid = FarmersDelight.MODID, value = Dist.CLIENT)
public class FDRecipes
{
	private static RecipeMap received = RecipeMap.EMPTY;

	// HIGHEST so the map is in place before JEI reloads on the same event
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void onRecipesReceived(RecipesReceivedEvent event) {
		received = event.getRecipeMap();
	}

	public List<RecipeHolder<CookingPotRecipe>> getCookingPotRecipes() {
		return List.copyOf(received.byType(ModRecipeTypes.COOKING.get()));
	}

	public List<RecipeHolder<CuttingBoardRecipe>> getCuttingBoardRecipes() {
		return List.copyOf(received.byType(ModRecipeTypes.CUTTING.get()));
	}

	/** The water-bucket dough recipe is a custom type that JEI cannot show, so it is shown as a shapeless craft. */
	public List<RecipeHolder<CraftingRecipe>> getSpecialCraftingRecipes() {
		return List.of(new RecipeHolder<>(RecipeUtils.FDKey("wheat_dough_from_water"), new ShapelessRecipe(
				new Recipe.CommonInfo(false), new CraftingRecipe.CraftingBookInfo(CraftingBookCategory.MISC, "fd_dough"),
				new ItemStackTemplate(ModItems.WHEAT_DOUGH.get()), List.of(Ingredient.of(Items.WHEAT), Ingredient.of(Items.WATER_BUCKET)))));
	}
}
