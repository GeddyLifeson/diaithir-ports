package org.dimdev.dimdoors.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import java.util.List;

public class TesselatingShapelessRecipe implements TesselatingRecipe {
    final String group;
    final ItemStack result;
    final List<Ingredient> ingredients;
    private final int weavingTime;
    private PlacementInfo placementInfo;

    public TesselatingShapelessRecipe(String group, ItemStack result, List<Ingredient> ingredients, int weavingTime) {
        this.group = group;
        this.result = result;
        this.ingredients = ingredients;
        this.weavingTime = weavingTime;
    }

    @Override
    public RecipeSerializer<? extends TesselatingShapelessRecipe> getSerializer() {
        return ModRecipeSerializers.SHAPELESS_TESSELATING;
    }

    @Override
    public String group() {
        return this.group;
    }

    @Override
    public boolean showNotification() {
        return true;
    }

    public List<Ingredient> getIngredients() {
        return this.ingredients;
    }

    public ItemStack getResult() {
        return this.result;
    }

    @Override
    public PlacementInfo placementInfo() {
        if (this.placementInfo == null) {
            this.placementInfo = PlacementInfo.create(this.ingredients);
        }
        return this.placementInfo;
    }

    @Override
    public boolean matches(CraftingInput craftingInput, Level level) {
        if (craftingInput.ingredientCount() != this.ingredients.size()) {
            return false;
        } else {
            return craftingInput.size() == 1 && this.ingredients.size() == 1 ? this.ingredients.getFirst().test(craftingInput.getItem(0)) : craftingInput.stackedContents().canCraft(this, null);
        }
    }

    @Override
    public int weavingTime() {
        return weavingTime;
    }

    @Override
    public ItemStack assemble(CraftingInput container) {
        return this.result.copy();
    }

    public static class Serializer {
        private static final MapCodec<TesselatingShapelessRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.optionalFieldOf("group", "").forGetter((shapelessRecipe) -> shapelessRecipe.group),
                ItemStack.CODEC.fieldOf("result").forGetter((shapelessRecipe) -> shapelessRecipe.result),
                Ingredient.CODEC.listOf(1, 9).fieldOf("ingredients").forGetter(shapelessRecipe -> shapelessRecipe.ingredients),
                ExtraCodecs.NON_NEGATIVE_INT.fieldOf("weaving_time").forGetter(TesselatingShapelessRecipe::weavingTime))
                .apply(instance, TesselatingShapelessRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, TesselatingShapelessRecipe> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8, r -> r.group,
                ItemStack.STREAM_CODEC, r -> r.result,
                Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()), r -> r.ingredients,
                ByteBufCodecs.INT, r -> r.weavingTime,
                TesselatingShapelessRecipe::new);

        public static RecipeSerializer<TesselatingShapelessRecipe> create() {
            return new RecipeSerializer<>(CODEC, STREAM_CODEC);
        }
    }
}
