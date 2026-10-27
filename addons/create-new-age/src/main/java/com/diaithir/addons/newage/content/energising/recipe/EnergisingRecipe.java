package com.diaithir.addons.newage.content.energising.recipe;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.content.processing.recipe.ProcessingOutput;
import com.zurrtum.create.foundation.recipe.CreateSingleStackRollableRecipe;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

import com.diaithir.addons.newage.CNARecipeTypes;

/** Charges an item on an energiser until the given amount of energy has been spent. */
public record EnergisingRecipe(List<ProcessingOutput> results, Ingredient ingredient, int energyNeeded) implements CreateSingleStackRollableRecipe {
    public static final MapCodec<EnergisingRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            ProcessingOutput.CODEC.listOf(1, 4).fieldOf("results").forGetter(EnergisingRecipe::results),
            Ingredient.CODEC.fieldOf("ingredient").forGetter(EnergisingRecipe::ingredient),
            Codec.INT.optionalFieldOf("energy_needed", 0).forGetter(EnergisingRecipe::energyNeeded)
    ).apply(i, EnergisingRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, EnergisingRecipe> STREAM_CODEC = StreamCodec.composite(
            ProcessingOutput.STREAM_CODEC.apply(ByteBufCodecs.list()), EnergisingRecipe::results,
            Ingredient.CONTENTS_STREAM_CODEC, EnergisingRecipe::ingredient,
            ByteBufCodecs.INT, EnergisingRecipe::energyNeeded,
            EnergisingRecipe::new);

    public static final RecipeSerializer<EnergisingRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    @Override
    public RecipeSerializer<EnergisingRecipe> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public RecipeType<EnergisingRecipe> getType() {
        return CNARecipeTypes.ENERGISING.get();
    }

    public int getEnergyNeeded() {
        return energyNeeded;
    }

    public boolean test(ItemStack stack) {
        return ingredient.test(stack);
    }
}
