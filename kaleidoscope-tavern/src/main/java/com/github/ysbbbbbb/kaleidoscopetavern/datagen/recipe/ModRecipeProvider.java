package com.github.ysbbbbbb.kaleidoscopetavern.datagen.recipe;

import com.github.ysbbbbbb.kaleidoscopetavern.KaleidoscopeTavern;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.ItemLike;

import java.util.Arrays;

public abstract class ModRecipeProvider extends RecipeProvider {
    public ModRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
        super(registries, output);
    }

    @Override
    protected abstract void buildRecipes();

    public Identifier modLoc(String path) {
        return KaleidoscopeTavern.modLoc(path);
    }

    public String getRecipeIdWithCount(ItemLike itemLike, int count) {
        return RecipeBuilder.getDefaultRecipeId(new ItemStackTemplate(itemLike.asItem())).identifier().getPath() + "_" + count;
    }

    public ItemLike[] getItemsWithCount(ItemLike itemLike, int count) {
        ItemLike[] items = new ItemLike[count];
        Arrays.fill(items, itemLike);
        return items;
    }
}
