package de.ellpeck.actuallyadditions.mod.crafting;

import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.PlacementInfo;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;

public class MiningLensRecipe implements Recipe<RecipeInput> {
    public static final String NAME = "mining_lens";

    private final int weight;
    private final Ingredient input;
    //private final int weight;
    private final ItemStack output;

    public MiningLensRecipe(Ingredient input, int weight, ItemStack output) {
        this.weight = weight;
        this.input = input;
        //this.weight = weight;
        this.output = output;
    }

    public int getWeight() {
        return weight;
    }

    public Ingredient getInput() {
        return input;
    }

    public boolean matches(ItemStack test) {
        return input.test(test);
    }

    @Override
    public boolean matches(RecipeInput pInv, Level pLevel) {
        return false;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Nonnull
    @Override
    public ItemStack assemble(RecipeInput pInv) {
        return ItemStack.EMPTY;
    }


    public ItemStack getResultItem(HolderLookup.Provider provider) {
        return output;
    }

    @Override
    public RecipeSerializer<? extends Recipe<RecipeInput>> getSerializer() {
        return ActuallyRecipes.MINING_LENS_RECIPE.get();
    }

    @Override
    public RecipeType<? extends Recipe<RecipeInput>> getType() {
        return ActuallyRecipes.Types.MINING_LENS.get();
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
        private static final MapCodec<MiningLensRecipe> CODEC = RecordCodecBuilder.mapCodec(
                instance -> instance.group(
                                Ingredient.CODEC.fieldOf("ingredient").forGetter(recipe -> recipe.input),
                                Codec.INT.fieldOf("weight").forGetter(recipe -> recipe.weight),
                                ItemStack.CODEC.fieldOf("result").forGetter(recipe -> recipe.output)
                        )
                        .apply(instance, MiningLensRecipe::new)
        );
        public static final StreamCodec<RegistryFriendlyByteBuf, MiningLensRecipe> STREAM_CODEC = StreamCodec.of(
                MiningLensRecipe.Serializer::toNetwork, MiningLensRecipe.Serializer::fromNetwork
        );
        public static final RecipeSerializer<MiningLensRecipe> INSTANCE = new RecipeSerializer<>(CODEC, STREAM_CODEC);

        public MapCodec<MiningLensRecipe> codec() {
            return CODEC;
        }

        public StreamCodec<RegistryFriendlyByteBuf, MiningLensRecipe> streamCodec() {
            return STREAM_CODEC;
        }

//        @Override
//        public MiningLensRecipe fromJson(@Nonnull Identifier pRecipeId, @Nonnull JsonObject pJson) {
//            Ingredient ingredient = Ingredient.fromJson(GsonHelper.getAsJsonObject(pJson, "ingredient"));
//            int weight = GsonHelper.getAsInt(pJson, "weight");
//            JsonObject resultObject = GsonHelper.getAsJsonObject(pJson, "result");
//            ItemStack result = new ItemStack(GsonHelper.getAsItem(resultObject, "item"));
//
//            return new MiningLensRecipe(pRecipeId, ingredient, weight, result);
//        }

        public static MiningLensRecipe fromNetwork(@Nonnull RegistryFriendlyByteBuf pBuffer) {
            Ingredient ingredient = Ingredient.CONTENTS_STREAM_CODEC.decode(pBuffer);
            int weight = pBuffer.readInt();
            ItemStack result = ItemStack.STREAM_CODEC.decode(pBuffer);
            return new MiningLensRecipe(ingredient, weight, result);
        }

        public static void toNetwork(@Nonnull RegistryFriendlyByteBuf pBuffer, MiningLensRecipe pRecipe) {
            Ingredient.CONTENTS_STREAM_CODEC.encode(pBuffer, pRecipe.input);
            pBuffer.writeInt(pRecipe.weight);
            ItemStack.STREAM_CODEC.encode(pBuffer, pRecipe.output);
        }
    }
}
