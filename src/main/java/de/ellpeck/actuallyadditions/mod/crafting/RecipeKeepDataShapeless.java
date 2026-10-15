/*
 * This file ("RecipeKeepDataShapeless.java") is part of the Actually Additions mod for Minecraft.
 * It is created and owned by Ellpeck and distributed
 * under the Actually Additions License to be found at
 * http://ellpeck.de/actaddlicense
 * View the source code at https://github.com/Ellpeck/ActuallyAdditions
 *
 * © 2015-2017 Ellpeck
 */

package de.ellpeck.actuallyadditions.mod.crafting;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.ellpeck.actuallyadditions.mod.ActuallyAdditions;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;

import java.util.List;

/** A shapeless recipe whose result takes the components of the ingredient marked with a TargetComponentIngredient; without one it makes nothing. */
public class RecipeKeepDataShapeless extends ShapelessRecipe {
    public static String NAME = "copy_nbt_shapeless";
    private final Recipe.CommonInfo info;
    private final CraftingRecipe.CraftingBookInfo book;
    private final ItemStackTemplate result;
    private final List<Ingredient> ingredients;

    public RecipeKeepDataShapeless(Recipe.CommonInfo info, CraftingRecipe.CraftingBookInfo book, ItemStackTemplate result, List<Ingredient> ingredients) {
        super(info, book, result, ingredients);
        this.info = info;
        this.book = book;
        this.result = result;
        this.ingredients = ingredients;
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        ItemStack craftingResult = super.assemble(input);
        if (!KeepData.copyFromDonor(craftingResult, this.ingredients, input)) {
            ActuallyAdditions.LOGGER.info("AA.KeepDataShapeless missing TargetComponentIngredient");
            return ItemStack.EMPTY;
        }
        return craftingResult;
    }

    @SuppressWarnings("unchecked")
    @Override
    public RecipeSerializer<ShapelessRecipe> getSerializer() {
        return (RecipeSerializer<ShapelessRecipe>) (RecipeSerializer<?>) Serializer.INSTANCE;
    }

    public static class Serializer {
        private static final MapCodec<RecipeKeepDataShapeless> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                Recipe.CommonInfo.MAP_CODEC.forGetter(o -> o.info),
                CraftingRecipe.CraftingBookInfo.MAP_CODEC.forGetter(o -> o.book),
                ItemStackTemplate.CODEC.fieldOf("result").forGetter(o -> o.result),
                Ingredient.CODEC.listOf(1, 9).fieldOf("ingredients").forGetter(o -> o.ingredients))
            .apply(i, RecipeKeepDataShapeless::new));
        private static final StreamCodec<RegistryFriendlyByteBuf, RecipeKeepDataShapeless> STREAM_CODEC = StreamCodec.composite(
            Recipe.CommonInfo.STREAM_CODEC, o -> o.info,
            CraftingRecipe.CraftingBookInfo.STREAM_CODEC, o -> o.book,
            ItemStackTemplate.STREAM_CODEC, o -> o.result,
            Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()), o -> o.ingredients,
            RecipeKeepDataShapeless::new);
        public static final RecipeSerializer<RecipeKeepDataShapeless> INSTANCE = new RecipeSerializer<>(CODEC, STREAM_CODEC);
    }
}
