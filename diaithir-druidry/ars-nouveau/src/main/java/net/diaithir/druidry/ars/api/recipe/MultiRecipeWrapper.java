package net.diaithir.druidry.ars.api.recipe;

import net.diaithir.druidry.ars.api.ArsNouveauAPI;
import net.diaithir.druidry.ars.common.block.tile.WixieCauldronTile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Stores a list of recipes and provides methods to check if a given inventory contains the items needed to craft any of the recipes.
 * These recipes typically all correspond to the same output item.
 */
public class MultiRecipeWrapper implements IRecipeWrapper{

    public Set<SingleRecipe> recipes;

    public static Map<Item, MultiRecipeWrapper> RECIPE_CACHE = new HashMap<>();

    public MultiRecipeWrapper() {
        recipes = new HashSet<>();
    }

    private static List<ItemStack> stacksOf(Ingredient ingredient) {
        List<ItemStack> out = new ArrayList<>();
        ingredient.items().forEach(h -> out.add(new ItemStack(h)));
        return out;
    }

    private static ItemStack resultOf(Recipe<?> r) {
        if (r instanceof ShapedRecipe s) {
            return s.assemble(CraftingInput.EMPTY);
        }
        if (r instanceof ShapelessRecipe s) {
            return s.assemble(CraftingInput.EMPTY);
        }
        return ItemStack.EMPTY;
    }

    public static MultiRecipeWrapper fromStack(ItemStack stack, Level level){
        MultiRecipeWrapper wrapper = new MultiRecipeWrapper();
        if (stack.getItem() == Items.POTION) {
            PotionBrewing brewing = level.potionBrewing();
            List<ItemStack> ingredientStacks = new ArrayList<>();
            for (Item item : BuiltInRegistries.ITEM) {
                ItemStack ing = new ItemStack(item);
                if (brewing.isIngredient(ing)) {
                    ingredientStacks.add(ing);
                }
            }
            for (Holder.Reference<Potion> potion : BuiltInRegistries.POTION.listElements().toList()) {
                ItemStack input = new ItemStack(Items.POTION);
                input.set(DataComponents.POTION_CONTENTS, new PotionContents(potion));
                for (ItemStack ing : ingredientStacks) {
                    ItemStack output = brewing.mix(ing, input);
                    if (output.isEmpty() || ItemStack.matches(output, input)) continue;
                    if (ItemStack.matches(stack, output)) {
                        List<Ingredient> list = new ArrayList<>();
                        list.add(PotionIngredient.getIngredient(input));
                        list.add(Ingredient.of(ing.getItem()));
                        wrapper.addRecipe(list, output, null);
                    }
                }
            }
        } else {
            if(RECIPE_CACHE.containsKey(stack.getItem())){
                return RECIPE_CACHE.get(stack.getItem());
            }
            for (RecipeHolder<?> rh : level.getServer().getRecipeManager().getRecipes()) {
                Recipe<?> r = rh.value();
                ItemStack result = resultOf(r);
                if (result == null || result.isEmpty() || result.getItem() != stack.getItem())
                    continue;

                if (r instanceof ShapedRecipe) {
                    ShapedHelper helper = new ShapedHelper((ShapedRecipe) r);
                    for (List<Ingredient> iList : helper.getPossibleRecipes()) {
                        wrapper.addRecipe(iList, result, r);
                    }
                }

                if (r instanceof ShapelessRecipe sl)
                    wrapper.addRecipe(new ArrayList<>(sl.placementInfo().ingredients()), result, r);

            }
            RECIPE_CACHE.put(stack.getItem(), wrapper);
        }
        return wrapper;
    }

    @Nullable
    @Override
    public InstructionsForRecipe canCraft(Map<Item, Integer> inventory, Level world, BlockPos pos) {
        for (SingleRecipe recipe : recipes) {
            List<ItemStack> itemsNeeded = getItemsNeeded(inventory, world, pos, recipe);
            if (itemsNeeded != null) {
                return new InstructionsForRecipe(recipe, itemsNeeded);
            }
        }
        return null;
    }

    /**
     * Returns a list of items needed to craft the recipe, or null if the recipe can't be crafted.
     * This method tracks that the inventory has enough of each item to craft the recipe.
     */
    @Nullable
    public List<ItemStack> getItemsNeeded(Map<Item, Integer> inventory, Level world, BlockPos pos, SingleRecipe recipe){
        Map<Item, Integer> map = new HashMap<>(inventory);

        List<ItemStack> items = new ArrayList<>();
        for (Ingredient i : recipe.recipeIngredients) {
            boolean foundStack = false;
            for (ItemStack stack : stacksOf(i)) {
                // Return success if we could consume this potion as a liquid from a jar
                if (stack.getItem() == Items.POTION) {
                    PotionContents potionContents = stack.get(DataComponents.POTION_CONTENTS);
                    if (potionContents == null) continue;

                    if (potionContents.is(Potions.WATER) || WixieCauldronTile.findNeededPotion(potionContents, 300, world, pos) != null) {
                        foundStack = true;
                        break;
                    }
                }
                // If our inventory has the item, decrease the effective count
                if (inventory.containsKey(stack.getItem()) && map.get(stack.getItem()) > 0) {
                    map.put(stack.getItem(), map.get(stack.getItem()) - 1);
                    foundStack = true;
                    items.add(stack.copy());
                    break;
                }
            }
            if (!foundStack)
                return null;
        }
        return items;
    }


    public boolean addRecipe(List<Ingredient> recipe, ItemStack outputStack, Recipe iRecipe) {
        return recipes.add(new SingleRecipe(recipe, outputStack, iRecipe));
    }

    public boolean isEmpty(){
        return recipes.isEmpty();
    }
}
