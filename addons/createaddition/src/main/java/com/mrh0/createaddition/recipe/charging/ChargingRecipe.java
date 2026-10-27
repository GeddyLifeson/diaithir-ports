package com.mrh0.createaddition.recipe.charging;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mrh0.createaddition.index.CARecipes;
import com.zurrtum.create.foundation.recipe.CreateSingleStackRecipe;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

/** Tesla coil charging: an item soaks up `energy` FE (at most `maxChargeRate` per tick) and becomes `result`. */
public record ChargingRecipe(ItemStackTemplate result, Ingredient ingredient, int energy, int maxChargeRate) implements CreateSingleStackRecipe {
    public static final MapCodec<ChargingRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
        ItemStackTemplate.CODEC.fieldOf("result").forGetter(ChargingRecipe::result),
        Ingredient.CODEC.fieldOf("ingredient").forGetter(ChargingRecipe::ingredient),
        com.mojang.serialization.Codec.INT.fieldOf("energy").forGetter(ChargingRecipe::energy),
        com.mojang.serialization.Codec.INT.optionalFieldOf("max_charge_rate", 0).forGetter(ChargingRecipe::maxChargeRate)
    ).apply(i, ChargingRecipe::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, ChargingRecipe> STREAM_CODEC = StreamCodec.composite(
        ItemStackTemplate.STREAM_CODEC, ChargingRecipe::result,
        Ingredient.CONTENTS_STREAM_CODEC, ChargingRecipe::ingredient,
        ByteBufCodecs.VAR_INT, ChargingRecipe::energy,
        ByteBufCodecs.VAR_INT, ChargingRecipe::maxChargeRate,
        ChargingRecipe::new
    );
    public static final RecipeSerializer<ChargingRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    public int getEnergy() { return energy; }
    public int getMaxChargeRate() { return maxChargeRate; }

    @Override public RecipeSerializer<ChargingRecipe> getSerializer() { return CARecipes.CHARGING; }
    @Override public RecipeType<ChargingRecipe> getType() { return CARecipes.CHARGING_TYPE; }
}
