package de.ellpeck.actuallyadditions.mod.crafting;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;

import java.util.Optional;

/** A shaped recipe whose result takes the components of the ingredient marked with a TargetComponentIngredient. */
public class RecipeKeepDataShaped extends ShapedRecipe {
    public static String NAME = "copy_nbt";
    private final Recipe.CommonInfo info;
    private final CraftingRecipe.CraftingBookInfo book;
    private final ItemStackTemplate result;

    public RecipeKeepDataShaped(Recipe.CommonInfo info, CraftingRecipe.CraftingBookInfo book, ShapedRecipePattern pattern, ItemStackTemplate result) {
        super(info, book, pattern, result);
        this.info = info;
        this.book = book;
        this.result = result;
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        ItemStack craftingResult = super.assemble(input);
        KeepData.copyFromDonor(craftingResult, this.getIngredients().stream().flatMap(Optional::stream).toList(), input);
        return craftingResult;
    }

    // the vanilla method is typed to vanilla's serializer; the recipe codec only needs the right instance
    @SuppressWarnings("unchecked")
    @Override
    public RecipeSerializer<ShapedRecipe> getSerializer() {
        return (RecipeSerializer<ShapedRecipe>) (RecipeSerializer<?>) Serializer.INSTANCE;
    }

    public static class Serializer {
        private static final MapCodec<RecipeKeepDataShaped> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                Recipe.CommonInfo.MAP_CODEC.forGetter(o -> o.info),
                CraftingRecipe.CraftingBookInfo.MAP_CODEC.forGetter(o -> o.book),
                ShapedRecipePattern.MAP_CODEC.forGetter(o -> o.pattern),
                ItemStackTemplate.CODEC.fieldOf("result").forGetter(o -> o.result))
            .apply(i, RecipeKeepDataShaped::new));
        private static final StreamCodec<RegistryFriendlyByteBuf, RecipeKeepDataShaped> STREAM_CODEC = StreamCodec.composite(
            Recipe.CommonInfo.STREAM_CODEC, o -> o.info,
            CraftingRecipe.CraftingBookInfo.STREAM_CODEC, o -> o.book,
            ShapedRecipePattern.STREAM_CODEC, o -> o.pattern,
            ItemStackTemplate.STREAM_CODEC, o -> o.result,
            RecipeKeepDataShaped::new);
        public static final RecipeSerializer<RecipeKeepDataShaped> INSTANCE = new RecipeSerializer<>(CODEC, STREAM_CODEC);
    }
}
