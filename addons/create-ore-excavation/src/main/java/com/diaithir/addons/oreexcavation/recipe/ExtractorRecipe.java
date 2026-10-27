package com.diaithir.addons.oreexcavation.recipe;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.infrastructure.fluids.FluidStack;

import com.diaithir.addons.oreexcavation.CreateOreExcavation;
import com.diaithir.addons.oreexcavation.recipe.ExcavatingRecipe.ExcavatingRecipeCommon;

public class ExtractorRecipe extends ExcavatingRecipe {
	public FluidStack output = FluidStack.EMPTY;

	public FluidStack getOutput() {
		return output;
	}

	public static final MapCodec<ExtractorRecipe> CODEC = RecordCodecBuilder.mapCodec(b -> b.group(
			ExcavatingRecipeCommon.CODEC.forGetter(ExtractorRecipe::getCommon),
			FluidStack.CODEC.fieldOf("output").forGetter(r -> r.output)
			).apply(b, (c, o) -> {
				ExtractorRecipe r = new ExtractorRecipe();
				r.setFromCommon(c);
				r.output = o;
				return r;
			}));

	public static final StreamCodec<RegistryFriendlyByteBuf, ExtractorRecipe> STREAM_CODEC = StreamCodec.of(
			(buf, r) -> {
				r.getCommon().write(buf);
				FluidStack.PACKET_CODEC.encode(buf, r.output);
			}, buf -> {
				ExtractorRecipe r = new ExtractorRecipe();
				r.setFromCommon(ExcavatingRecipeCommon.read(buf));
				r.output = FluidStack.PACKET_CODEC.decode(buf);
				return r;
			});

	public static final RecipeSerializer<ExtractorRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);

	@Override
	public RecipeSerializer<ExtractorRecipe> getSerializer() {
		return SERIALIZER;
	}

	@Override
	public RecipeType<ExtractorRecipe> getType() {
		return CreateOreExcavation.EXTRACTING_RECIPES.getRecipeType();
	}
}
