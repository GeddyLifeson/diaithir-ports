package net.diaithir.druidry.ars.common.crafting.recipes;

import net.diaithir.druidry.ars.api.potion.IPotionProvider;
import net.diaithir.druidry.ars.api.registry.PotionProviderRegistry;
import net.diaithir.druidry.ars.common.items.PotionFlask;
import net.diaithir.druidry.ars.common.util.PotionUtil;
import net.diaithir.druidry.ars.setup.registry.RecipeRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStackTemplate;
import java.util.List;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.crafting.*;

import java.util.List;

public class PotionFlaskRecipe extends ShapelessRecipe {

    public PotionFlaskRecipe(Recipe.CommonInfo commonInfo, CraftingRecipe.CraftingBookInfo bookInfo, ItemStackTemplate result, List<Ingredient> ingredients) {
        super(commonInfo, bookInfo, result, ingredients);
    }

    public PotionFlaskRecipe(String groupIn, ItemStack result, ItemStack recipeOutputIn) {
        super(new Recipe.CommonInfo(true), new CraftingRecipe.CraftingBookInfo(CraftingBookCategory.MISC, groupIn),
                ItemStackTemplate.fromNonEmptyStack(recipeOutputIn), List.of(Ingredient.of(result.getItem()), Ingredient.of(Items.POTION)));
    }



    @Override
    public ItemStack assemble(final CraftingInput inv) {
        final ItemStack output = super.assemble(inv);
        if (output.isEmpty())
            return ItemStack.EMPTY;
        ItemStack flaskPotionStack = ItemStack.EMPTY;
        ItemStack potionStack = ItemStack.EMPTY;
        for (int i = 0; i < inv.size(); i++) {
            final ItemStack stack = inv.getItem(i);
            if (stack.getItem() instanceof PotionFlask flask) {
                flaskPotionStack = stack.copy();
                IPotionProvider provider = PotionProviderRegistry.from(flaskPotionStack);
                if(provider == null || provider.roomLeft(flaskPotionStack) <= 0)
                    return ItemStack.EMPTY;
            }
            if(stack.getItem() instanceof PotionItem){
                potionStack = stack;
            }
        }
        if(flaskPotionStack.isEmpty() || potionStack.isEmpty())
            return ItemStack.EMPTY;
        IPotionProvider provider = PotionProviderRegistry.from(flaskPotionStack);
        PotionContents potionData = potionStack.get(DataComponents.POTION_CONTENTS);
        if(provider == null)
            return ItemStack.EMPTY;
        int count = provider.usesRemaining(flaskPotionStack);
        ItemStack copyStack = flaskPotionStack.copy();
        if(count <= 0){
            provider.setData(potionData, 1, provider.maxUses(flaskPotionStack), copyStack);
            return copyStack;
        }else if(PotionUtil.arePotionContentsEqual(potionData, provider.getPotionData(potionStack))){
            provider.setData(potionData, count + 1, provider.maxUses(flaskPotionStack), copyStack);
            return copyStack;
        }
        return ItemStack.EMPTY; // Return the modified output
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput inv) {
        NonNullList<ItemStack> nonnulllist = CraftingRecipe.defaultCraftingReminder(inv);
        for (int i = 0; i < nonnulllist.size(); ++i) {
            if (nonnulllist.get(i).isEmpty() && inv.getItem(i).getItem() instanceof PotionItem) {
                nonnulllist.set(i, new ItemStack(Items.GLASS_BOTTLE));
            }
        }
        return nonnulllist;
    }

    @Override
    @SuppressWarnings("unchecked")
    public RecipeSerializer<ShapelessRecipe> getSerializer() {
        return (RecipeSerializer<ShapelessRecipe>) (RecipeSerializer<?>) RecipeRegistry.POTION_FLASK_RECIPE.get();
    }
}
