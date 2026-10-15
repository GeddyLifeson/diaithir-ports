package de.ellpeck.actuallyadditions.mod.crafting;

import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.PlacementInfo;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.ellpeck.actuallyadditions.api.ActuallyAdditionsAPI;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

public class ColorChangeRecipe implements Recipe<SingleRecipeInput> {
    public static final String NAME = "color_change";

    private final Ingredient input;
    private final ItemStack output;

    public ColorChangeRecipe(ItemStack output, Ingredient input) {
        this.input = input;
        this.output = output;
    }

    @Override
    public boolean matches(@Nonnull SingleRecipeInput input, @Nonnull Level pLevel) {
        return this.input.test(input.getItem(0));
    }

    public boolean matches(ItemStack stack) {
        return input.test(stack);
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Nonnull
    @Override
    public ItemStack assemble(SingleRecipeInput input) {
        return output.copy();
    }


    public static Optional<RecipeHolder<ColorChangeRecipe>> getRecipeForStack(ItemStack stack) {
        return ActuallyAdditionsAPI.COLOR_CHANGE_RECIPES.stream().filter(recipe -> recipe.value().matches(stack)).findFirst();
    }

    @Nonnull
    public ItemStack getResultItem(HolderLookup.Provider provider) {
        return output.copy();
    }

    @Nonnull
    @Override
    public RecipeSerializer<? extends Recipe<SingleRecipeInput>> getSerializer() {
        return ActuallyRecipes.COLOR_CHANGE_RECIPE.get();
    }

    @Override
    public RecipeType<? extends Recipe<SingleRecipeInput>> getType() {
        return ActuallyRecipes.Types.COLOR_CHANGE.get();
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public String group() {
        return "";
    }

    // machine recipes: never placed by the recipe book
    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }

    public static class Serializer {
        private static final MapCodec<ColorChangeRecipe> CODEC = RecordCodecBuilder.mapCodec(
                instance -> instance.group(
                                ItemStack.CODEC.fieldOf("result").forGetter(recipe -> recipe.output),
                                Ingredient.CODEC.fieldOf("ingredient").forGetter(recipe -> recipe.input)
                        )
                        .apply(instance, ColorChangeRecipe::new)
        );
        public static final StreamCodec<RegistryFriendlyByteBuf, ColorChangeRecipe> STREAM_CODEC = StreamCodec.of(
                ColorChangeRecipe.Serializer::toNetwork, ColorChangeRecipe.Serializer::fromNetwork
        );
        public static final RecipeSerializer<ColorChangeRecipe> INSTANCE = new RecipeSerializer<>(CODEC, STREAM_CODEC);

        public MapCodec<ColorChangeRecipe> codec() {
            return CODEC;
        }

        public StreamCodec<RegistryFriendlyByteBuf, ColorChangeRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        @Nullable
        public static ColorChangeRecipe fromNetwork(@Nonnull RegistryFriendlyByteBuf pBuffer) {
            Ingredient ingredient = Ingredient.CONTENTS_STREAM_CODEC.decode(pBuffer);

            ItemStack result = ItemStack.STREAM_CODEC.decode(pBuffer);
            return new ColorChangeRecipe(result, ingredient);
        }

        public static void toNetwork(@Nonnull RegistryFriendlyByteBuf pBuffer, ColorChangeRecipe pRecipe) {
            Ingredient.CONTENTS_STREAM_CODEC.encode(pBuffer, pRecipe.input);
            ItemStack.STREAM_CODEC.encode(pBuffer, pRecipe.output);
        }
    }
}
