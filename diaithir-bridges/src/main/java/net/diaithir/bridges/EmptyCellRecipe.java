package net.diaithir.bridges;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
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
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * A shapeless crafting recipe (same JSON as minecraft:crafting_shapeless, type diaithirbridges:empty_cell_convert) that does not match while
 * any input stack is a storage cell or disk with stored contents (see {@link CellGuard}). Use it for AE2 cell to Refined Storage disk
 * conversion so a full one is refused rather than destroyed.
 */
public final class EmptyCellRecipe extends ShapelessRecipe {
    public static final MapCodec<EmptyCellRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Recipe.CommonInfo.MAP_CODEC.forGetter(o -> o.commonInfo),
            CraftingRecipe.CraftingBookInfo.MAP_CODEC.forGetter(o -> o.bookInfo),
            ItemStackTemplate.CODEC.fieldOf("result").forGetter(o -> o.template),
            Codec.lazyInitialized(() -> Ingredient.CODEC.listOf(1, 9)).fieldOf("ingredients").forGetter(o -> o.inputs)
    ).apply(i, EmptyCellRecipe::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, EmptyCellRecipe> STREAM_CODEC = StreamCodec.composite(
            Recipe.CommonInfo.STREAM_CODEC, o -> o.commonInfo,
            CraftingRecipe.CraftingBookInfo.STREAM_CODEC, o -> o.bookInfo,
            ItemStackTemplate.STREAM_CODEC, o -> o.template,
            Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()), o -> o.inputs,
            EmptyCellRecipe::new);
    public static final RecipeSerializer<EmptyCellRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    private final ItemStackTemplate template;
    private final List<Ingredient> inputs;

    EmptyCellRecipe(Recipe.CommonInfo common, CraftingRecipe.CraftingBookInfo book, ItemStackTemplate result, List<Ingredient> ingredients) {
        super(common, book, result, ingredients);
        this.template = result;
        this.inputs = ingredients;
    }

    @Override
    public RecipeSerializer<ShapelessRecipe> getSerializer() {
        @SuppressWarnings({"unchecked", "rawtypes"})
        RecipeSerializer<ShapelessRecipe> s = (RecipeSerializer) SERIALIZER;
        return s;
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        if (!super.matches(input, level)) return false;
        var guarded = CellGuard.configured();
        for (ItemStack s : input.items()) if (!s.isEmpty() && CellGuard.hasContents(s, guarded)) return false;
        return true;
    }
}
