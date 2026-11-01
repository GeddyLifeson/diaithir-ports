package com.gregtechceu.gtceu.api.recipe;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import com.mojang.serialization.MapCodec;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class StrictShapedRecipe extends ShapedRecipe {

    public StrictShapedRecipe(Recipe.CommonInfo commonInfo, CraftingRecipe.CraftingBookInfo bookInfo,
                              ShapedRecipePattern pattern, ItemStackTemplate result) {
        super(commonInfo, bookInfo, pattern, result);
    }

    @Override
    public boolean matches(CraftingInput inv, Level level) {
        for (int i = 0; i <= inv.width() - this.getWidth(); ++i) {
            for (int j = 0; j <= inv.height() - this.getHeight(); ++j) {
                if (this.matches(inv, i, j)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if the region of a crafting inventory is match for the recipe.
     */
    private boolean matches(CraftingInput inv, int width, int height) {
        for (int i = 0; i < inv.width(); ++i) {
            for (int j = 0; j < inv.height(); ++j) {
                int k = i - width;
                int l = j - height;
                Optional<Ingredient> ingredient = Optional.empty();
                if (k >= 0 && l >= 0 && k < this.getWidth() && l < this.getHeight()) {
                    ingredient = this.getIngredients().get(k + l * this.getWidth());
                }
                ItemStack stack = inv.getItem(i + j * inv.width());
                if (ingredient.isPresent() ? ingredient.get().test(stack) : stack.isEmpty()) continue;
                return false;
            }
        }
        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull RecipeSerializer<ShapedRecipe> getSerializer() {
        return (RecipeSerializer<ShapedRecipe>) (RecipeSerializer<?>) SERIALIZER_STRICT;
    }

    public static final MapCodec<StrictShapedRecipe> CODEC = ShapedRecipe.MAP_CODEC
            .xmap(StrictShapedRecipe::from, r -> r);
    public static final net.minecraft.network.codec.StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, StrictShapedRecipe> STREAM_CODEC = ShapedRecipe.STREAM_CODEC
            .map(StrictShapedRecipe::from, r -> r);
    public static final RecipeSerializer<StrictShapedRecipe> SERIALIZER_STRICT = new RecipeSerializer<>(CODEC,
            STREAM_CODEC);

    public static StrictShapedRecipe from(ShapedRecipe r) {
        return new StrictShapedRecipe(r.commonInfo, r.bookInfo, r.pattern, r.result);
    }
}
