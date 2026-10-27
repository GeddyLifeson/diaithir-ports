package com.diaithir.addons.oreexcavation.recipe;

import java.util.List;

import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.content.processing.recipe.ProcessingOutput;

import com.diaithir.addons.oreexcavation.CreateOreExcavation;
import com.diaithir.addons.oreexcavation.recipe.ExcavatingRecipe.ExcavatingRecipeCommon;

public class DrillingRecipe extends ExcavatingRecipe {
	public NonNullList<ProcessingOutput> output = NonNullList.create();

	public NonNullList<ProcessingOutput> getOutput() {
		return output;
	}

	public static final MapCodec<DrillingRecipe> CODEC = RecordCodecBuilder.mapCodec(b -> b.group(
			ExcavatingRecipeCommon.CODEC.forGetter(DrillingRecipe::getCommon),
			Codec.list(ProcessingOutput.CODEC).fieldOf("output").forGetter(r -> (List<ProcessingOutput>) r.output)
			).apply(b, (c, o) -> {
				DrillingRecipe r = new DrillingRecipe();
				r.setFromCommon(c);
				r.output = NonNullList.copyOf(o);
				return r;
			}));

	public static final StreamCodec<RegistryFriendlyByteBuf, DrillingRecipe> STREAM_CODEC = StreamCodec.of(
			(buf, r) -> {
				r.getCommon().write(buf);
				ProcessingOutput.STREAM_CODEC.apply(ByteBufCodecs.list()).encode(buf, r.output);
			}, buf -> {
				DrillingRecipe r = new DrillingRecipe();
				r.setFromCommon(ExcavatingRecipeCommon.read(buf));
				r.output = NonNullList.copyOf(ProcessingOutput.STREAM_CODEC.apply(ByteBufCodecs.list()).decode(buf));
				return r;
			});

	public static final RecipeSerializer<DrillingRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);

	@Override
	public RecipeSerializer<DrillingRecipe> getSerializer() {
		return SERIALIZER;
	}

	@Override
	public RecipeType<DrillingRecipe> getType() {
		return CreateOreExcavation.DRILLING_RECIPES.getRecipeType();
	}
}
