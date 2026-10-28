package cn.leolezury.eternalstarlight.common.item.recipe;

import cn.leolezury.eternalstarlight.common.EternalStarlight;
import cn.leolezury.eternalstarlight.common.registry.ESRecipeSerializers;
import cn.leolezury.eternalstarlight.common.registry.ESRecipes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentExactPredicate;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import java.util.List;

public record EtherConversionRecipe(Ingredient input, int inputCount, DataComponentExactPredicate components, List<WeightedOutput> outputs) implements Recipe<SingleRecipeInput> {
	@Override
	public boolean matches(SingleRecipeInput container, Level level) {
		ItemStack stack = container.getItem(0);
		return !stack.isEmpty() && stack.getCount() >= inputCount() && input().test(stack) && components().test(stack);
	}

	@Override
	public ItemStack assemble(SingleRecipeInput container) {
		return outputs().getFirst().output().copy();
	}



	public ItemStack rollOutput(RandomSource random) {
		int totalWeight = 0;
		for (WeightedOutput output : outputs()) {
			totalWeight += output.weight();
		}
		if (totalWeight <= 0) {
			return ItemStack.EMPTY;
		}
		int roll = random.nextInt(totalWeight);
		for (WeightedOutput output : outputs()) {
			roll -= output.weight();
			if (roll < 0) {
				return output.output().copy();
			}
		}
		return ItemStack.EMPTY;
	}

	@Override
	public boolean showNotification() {
		return true;
	}

	@Override
	public String group() {
		return "";
	}

	@Override
	public PlacementInfo placementInfo() {
		return PlacementInfo.NOT_PLACEABLE;
	}

	@Override
	public RecipeBookCategory recipeBookCategory() {
		return RecipeBookCategories.FURNACE_MISC;
	}

	@Override
	public RecipeSerializer<EtherConversionRecipe> getSerializer() {
		return ESRecipeSerializers.ETHER_CONVERSION.get();
	}

	@Override
	public RecipeType<EtherConversionRecipe> getType() {
		return ESRecipes.ETHER_CONVERSION.get();
	}

	public record WeightedOutput(ItemStackTemplate outputTemplate, int weight) {
		public ItemStack output() {
			return outputTemplate.create();
		}

		public static final Codec<WeightedOutput> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ItemStackTemplate.CODEC.fieldOf("output").forGetter(WeightedOutput::outputTemplate),
			Codec.INT.fieldOf("weight").forGetter(WeightedOutput::weight)
		).apply(instance, WeightedOutput::new));
	}

	public static class Type implements RecipeType<EtherConversionRecipe> {
		public static final Identifier ID = EternalStarlight.id("ether_conversion");

		@Override
		public String toString() {
			return ID.toString();
		}
	}

	public static class Serializer {
		public RecipeSerializer<EtherConversionRecipe> build() {
			return new RecipeSerializer<>(codec(), streamCodec());
		}

		private static final MapCodec<EtherConversionRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			Ingredient.CODEC.fieldOf("input").forGetter(EtherConversionRecipe::input),
			Codec.INT.optionalFieldOf("input_count", 1).forGetter(EtherConversionRecipe::inputCount),
			DataComponentExactPredicate.CODEC.optionalFieldOf("components", DataComponentExactPredicate.EMPTY).forGetter(EtherConversionRecipe::components),
			WeightedOutput.CODEC.listOf().fieldOf("outputs").forGetter(EtherConversionRecipe::outputs)
		).apply(instance, EtherConversionRecipe::new));

		public MapCodec<EtherConversionRecipe> codec() {
			return CODEC;
		}

		public StreamCodec<RegistryFriendlyByteBuf, EtherConversionRecipe> streamCodec() {
			return new StreamCodec<>() {
				@Override
				public EtherConversionRecipe decode(RegistryFriendlyByteBuf friendlyByteBuf) {
					Ingredient input = Ingredient.CONTENTS_STREAM_CODEC.decode(friendlyByteBuf);
					int inputCount = friendlyByteBuf.readInt();
					DataComponentExactPredicate components = DataComponentExactPredicate.STREAM_CODEC.decode(friendlyByteBuf);
					int outputCount = friendlyByteBuf.readVarInt();
					List<WeightedOutput> outputs = new java.util.ArrayList<>(outputCount);
					for (int i = 0; i < outputCount; i++) {
						ItemStackTemplate output = ItemStackTemplate.STREAM_CODEC.decode(friendlyByteBuf);
						int weight = friendlyByteBuf.readInt();
						outputs.add(new WeightedOutput(output, weight));
					}
					return new EtherConversionRecipe(input, inputCount, components, outputs);
				}

				@Override
				public void encode(RegistryFriendlyByteBuf friendlyByteBuf, EtherConversionRecipe recipe) {
					Ingredient.CONTENTS_STREAM_CODEC.encode(friendlyByteBuf, recipe.input());
					friendlyByteBuf.writeInt(recipe.inputCount());
					DataComponentExactPredicate.STREAM_CODEC.encode(friendlyByteBuf, recipe.components());
					friendlyByteBuf.writeVarInt(recipe.outputs().size());
					for (WeightedOutput output : recipe.outputs()) {
						ItemStackTemplate.STREAM_CODEC.encode(friendlyByteBuf, output.outputTemplate());
						friendlyByteBuf.writeInt(output.weight());
					}
				}
			};
		}
	}
}
