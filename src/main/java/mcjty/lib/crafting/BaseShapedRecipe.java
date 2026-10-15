package mcjty.lib.crafting;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.*;

public class BaseShapedRecipe extends ShapedRecipe {

    public BaseShapedRecipe(String group, CraftingBookCategory category, ShapedRecipePattern pattern, ItemStack stack) {
        super(new Recipe.CommonInfo(true), new CraftingRecipe.CraftingBookInfo(category, group), pattern,
                new ItemStackTemplate(stack.typeHolder(), stack.getCount(), stack.getComponentsPatch()));
    }

    public BaseShapedRecipe(ShapedRecipe recipe) {
        super(recipe.commonInfo, recipe.bookInfo, recipe.pattern, recipe.result);
    }
}
