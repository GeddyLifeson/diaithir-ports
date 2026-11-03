package net.diaithir.druidry.ars.common.crafting.recipes;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;

import java.util.List;

/**
 * Builds a serializer for a ShapelessRecipe subclass by mapping the vanilla shapeless codecs.
 */
public class ExtendableShapelessSerializer {

    public static <T extends ShapelessRecipe> RecipeSerializer<T> create(ShapelessConstructor<T> creator) {
        MapCodec<T> codec = ShapelessRecipe.MAP_CODEC.xmap(r -> convert(creator, r), t -> t);
        StreamCodec<RegistryFriendlyByteBuf, T> stream = ShapelessRecipe.STREAM_CODEC.map(r -> convert(creator, r), t -> t);
        return new RecipeSerializer<>(codec, stream);
    }

    private static <T extends ShapelessRecipe> T convert(ShapelessConstructor<T> creator, ShapelessRecipe r) {
        return creator.create(new Recipe.CommonInfo(r.showNotification()),
                new CraftingRecipe.CraftingBookInfo(r.category(), r.group()),
                r.result(), r.placementInfo().ingredients());
    }

    @FunctionalInterface
    public interface ShapelessConstructor<T extends ShapelessRecipe> {
        T create(Recipe.CommonInfo commonInfo, CraftingRecipe.CraftingBookInfo bookInfo, ItemStackTemplate result, List<Ingredient> ingredients);
    }
}
