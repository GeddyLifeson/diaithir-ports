package com.gregtechceu.gtceu.api.recipe;

import com.gregtechceu.gtceu.api.recipe.ingredient.FluidContainerIngredient;
import com.gregtechceu.gtceu.common.data.GTRecipeSerializers;


import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.IntObjectPair;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;

// TODO shapeless fluid container recipes
@NotNullByDefault
public class ShapedFluidContainerRecipe extends ShapedRecipe {

    public ShapedFluidContainerRecipe(String group, CraftingBookCategory category,
                                      ShapedRecipePattern pattern, ItemStack result,
                                      boolean showNotification) {
        super(group, category, pattern, result, showNotification);
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput inv) { return null; /* ported stub */ }

    /**
     * Checks if the region of a crafting inventory is match for the recipe.
     */
    @Nullable
    private IntObjectPair<ItemStack> findFluidReplacement(CraftingInput inv, int width, int height, boolean mirrored) { return null; /* ported stub */ }

    @Override
    public RecipeSerializer<?> getSerializer() { return null; /* ported stub */ }

    public static class Serializer {

        // spotless:off
        public static final MapCodec<ShapedFluidContainerRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.STRING.optionalFieldOf("group", "").forGetter(ShapedRecipe::getGroup),
                CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(ShapedRecipe::category),
                ShapedRecipePattern.MAP_CODEC.forGetter(i -> i.pattern),
                ItemStack.STRICT_CODEC.fieldOf("result").forGetter(val -> ((ShapedRecipeAccessor) val).getResult()),
                Codec.BOOL.optionalFieldOf("show_notification", true).forGetter(val -> ((ShapedRecipeAccessor) val).getShowNotification())
        ).apply(instance, ShapedFluidContainerRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, ShapedFluidContainerRecipe> STREAM_CODEC = StreamCodec.of(
                ShapedFluidContainerRecipe.Serializer::toNetwork, ShapedFluidContainerRecipe.Serializer::fromNetwork
        );
        // spotless:on

        @Override
        public MapCodec<ShapedFluidContainerRecipe> codec() { return null; /* ported stub */ }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, ShapedFluidContainerRecipe> streamCodec() { return null; /* ported stub */ }

        private static ShapedFluidContainerRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
            String group = buffer.readUtf();
            CraftingBookCategory category = buffer.readEnum(CraftingBookCategory.class);
            ShapedRecipePattern pattern = ShapedRecipePattern.STREAM_CODEC.decode(buffer);
            ItemStack output = ItemStack.STREAM_CODEC.decode(buffer);
            boolean showNotification = buffer.readBoolean();
            return new ShapedFluidContainerRecipe(group, category, pattern, output, showNotification);
        }

        private static void toNetwork(RegistryFriendlyByteBuf buffer, ShapedFluidContainerRecipe recipe) {  /* ported stub */ }
    }
}
