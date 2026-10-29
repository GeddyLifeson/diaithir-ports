package com.gregtechceu.gtceu.api.recipe;

import com.gregtechceu.gtceu.api.recipe.ingredient.FluidContainerIngredient;

import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.*;

import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.ints.IntObjectPair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

// TODO shapeless fluid container recipes
public class ShapedFluidContainerRecipe extends ShapedRecipe {

    public ShapedFluidContainerRecipe(Recipe.CommonInfo commonInfo, CraftingRecipe.CraftingBookInfo bookInfo,
                                      ShapedRecipePattern pattern, ItemStackTemplate result) {
        super(commonInfo, bookInfo, pattern, result);
    }

    @Override
    public @NotNull NonNullList<ItemStack> getRemainingItems(CraftingInput inv) {
        NonNullList<ItemStack> items = NonNullList.withSize(inv.size(), ItemStack.EMPTY);

        int replacedSlot = -1;
        OUTER_LOOP:
        for (int x = 0; x <= inv.width() - this.getWidth(); ++x) {
            for (int y = 0; y <= inv.height() - this.getHeight(); ++y) {
                var stack = this.findFluidReplacement(inv, x, y, false);
                if (stack != null) {
                    replacedSlot = stack.firstInt();
                    items.set(replacedSlot, stack.second());
                    break OUTER_LOOP;
                }
                stack = this.findFluidReplacement(inv, x, y, true);
                if (stack != null) {
                    replacedSlot = stack.firstInt();
                    items.set(replacedSlot, stack.second());
                    break OUTER_LOOP;
                }
            }
        }

        NonNullList<ItemStack> defaults = CraftingRecipe.defaultCraftingReminder(inv);
        for (int i = 0; i < items.size(); ++i) {
            if (i == replacedSlot) continue;
            items.set(i, defaults.get(i));
        }
        return items;
    }

    @Nullable
    private IntObjectPair<ItemStack> findFluidReplacement(CraftingInput inv, int width, int height,
                                                          boolean mirrored) {
        for (int x = 0; x < inv.width(); ++x) {
            for (int y = 0; y < inv.height(); ++y) {
                int offsetX = x - width;
                int offsetY = y - height;
                Optional<Ingredient> ingredient = Optional.empty();
                if (offsetX >= 0 && offsetY >= 0 && offsetX < this.getWidth() && offsetY < this.getHeight()) {
                    if (mirrored) {
                        ingredient = this.getIngredients()
                                .get(this.getWidth() - offsetX - 1 + offsetY * this.getWidth());
                    } else {
                        ingredient = this.getIngredients().get(offsetX + offsetY * this.getWidth());
                    }
                }
                if (ingredient.isPresent() && ingredient.get().isCustom() &&
                        ingredient.get().getCustomIngredient() instanceof FluidContainerIngredient fci) {
                    int slot = x + y * inv.width();
                    ItemStack stack = inv.getItem(slot);
                    if (fci.test(stack)) {
                        return IntObjectPair.of(slot, fci.getExtractedStack(stack));
                    }
                }
            }
        }
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull RecipeSerializer<ShapedRecipe> getSerializer() {
        return (RecipeSerializer<ShapedRecipe>) (RecipeSerializer<?>) SERIALIZER_FLUID;
    }

    public static ShapedFluidContainerRecipe from(ShapedRecipe r) {
        return new ShapedFluidContainerRecipe(r.commonInfo, r.bookInfo, r.pattern, r.result);
    }

    public static final MapCodec<ShapedFluidContainerRecipe> CODEC = ShapedRecipe.MAP_CODEC
            .xmap(ShapedFluidContainerRecipe::from, r -> r);
    public static final StreamCodec<RegistryFriendlyByteBuf, ShapedFluidContainerRecipe> STREAM_CODEC = ShapedRecipe.STREAM_CODEC
            .map(ShapedFluidContainerRecipe::from, r -> r);
    public static final RecipeSerializer<ShapedFluidContainerRecipe> SERIALIZER_FLUID = new RecipeSerializer<>(CODEC,
            STREAM_CODEC);
}
