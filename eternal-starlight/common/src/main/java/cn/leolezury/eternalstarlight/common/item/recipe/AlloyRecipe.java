package cn.leolezury.eternalstarlight.common.item.recipe;

import net.minecraft.util.valueproviders.IntProviders;

import cn.leolezury.eternalstarlight.common.EternalStarlight;
import cn.leolezury.eternalstarlight.common.registry.ESItems;
import cn.leolezury.eternalstarlight.common.registry.ESRecipeSerializers;
import cn.leolezury.eternalstarlight.common.registry.ESRecipes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

public record AlloyRecipe(NonNullList<Result> results, NonNullList<Ingredient> ingredients, int burnTime) implements Recipe<CraftingInput> {
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
	public RecipeSerializer<AlloyRecipe> getSerializer() {
		return ESRecipeSerializers.ALLOY.get();
	}

	@Override
	public RecipeType<AlloyRecipe> getType() {
		return ESRecipes.ALLOY.get();
	}

	public static class Type implements RecipeType<AlloyRecipe> {
		public static final Identifier ID = EternalStarlight.id("alloy");

		@Override
		public String toString() {
			return ID.toString();
		}
	}




	public net.minecraft.core.NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
		return net.minecraft.world.item.crafting.CraftingRecipe.defaultCraftingReminder(input);
	}

	private boolean matchesUnordered(CraftingInput input) {
		java.util.List<ItemStack> remaining = new java.util.ArrayList<>();
		for (ItemStack stack : input.items()) {
			if (!stack.isEmpty()) {
				remaining.add(stack);
			}
		}
		for (Ingredient ingredient : this.ingredients) {
			boolean found = false;
			for (int i = 0; i < remaining.size(); i++) {
				if (ingredient.test(remaining.get(i))) {
					remaining.remove(i);
					found = true;
					break;
				}
			}
			if (!found) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean matches(CraftingInput craftingInput, Level level) {
		if (craftingInput.ingredientCount() != this.ingredients.size()) {
			return false;
		} else {
			return craftingInput.size() == 1 && this.ingredients.size() == 1 ? this.ingredients.getFirst().test(craftingInput.getItem(0)) : matchesUnordered(craftingInput);
		}
	}

	@Override
	public ItemStack assemble(CraftingInput craftingInput) {
		return this.results.getFirst().item().copy();
	}


	public record Result(ItemStack item, IntProvider amount) {
		public static final Codec<Result> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
			ItemStack.CODEC.fieldOf("item").forGetter(Result::item),
			IntProviders.NON_NEGATIVE_CODEC.fieldOf("amount").forGetter(Result::amount)
		).apply(instance, Result::new));

		public static final StreamCodec<? super RegistryFriendlyByteBuf, Result> STREAM_CODEC = StreamCodec.composite(
			ItemStack.STREAM_CODEC, Result::item,
			ByteBufCodecs.fromCodec(IntProviders.NON_NEGATIVE_CODEC), Result::amount,
			Result::new
		);


		public ItemStack getResultItem(net.minecraft.util.RandomSource random) {
			return item().copyWithCount(amount().sample(random));
		}

		public ItemStack getMaxResultItem() {
			return item().copyWithCount(amount().maxInclusive());
		}
	}

	public static class Serializer {
		public RecipeSerializer<AlloyRecipe> build() {
			return new RecipeSerializer<>(codec(), streamCodec());
		}

		private static final MapCodec<AlloyRecipe> CODEC = RecordCodecBuilder.mapCodec((instance) -> instance.group(
			Result.CODEC.listOf().fieldOf("results").flatXmap((list) -> {
				Result[] items = list.stream().filter((stack) -> !stack.item().isEmpty()).toArray(Result[]::new);
				if (items.length == 0) {
					return DataResult.error(() -> "No result for alloy recipe");
				} else {
					return items.length > 3 ? DataResult.error(() -> "Too many results for alloy recipe") : DataResult.success(NonNullList.of(new Result(ItemStack.EMPTY, ConstantInt.of(0)), items));
				}
			}, DataResult::success).forGetter(AlloyRecipe::results),
			Ingredient.CODEC.listOf().fieldOf("ingredients").flatXmap((list) -> {
				Ingredient[] ingredients = list.stream().filter((ingredient) -> !ingredient.isEmpty()).toArray(Ingredient[]::new);
				if (ingredients.length == 0) {
					return DataResult.error(() -> "No ingredient for alloy recipe");
				} else {
					return ingredients.length > 9 ? DataResult.error(() -> "Too many ingredients for alloy recipe") : DataResult.success(NonNullList.of(ingredients[0], ingredients));
				}
			}, DataResult::success).forGetter(AlloyRecipe::ingredients),
			Codec.INT.fieldOf("burn_time").forGetter(AlloyRecipe::burnTime)
		).apply(instance, AlloyRecipe::new));
		public static final StreamCodec<RegistryFriendlyByteBuf, AlloyRecipe> STREAM_CODEC = StreamCodec.of(Serializer::toNetwork, Serializer::fromNetwork);

		public MapCodec<AlloyRecipe> codec() {
			return CODEC;
		}

		public StreamCodec<RegistryFriendlyByteBuf, AlloyRecipe> streamCodec() {
			return STREAM_CODEC;
		}

		private static AlloyRecipe fromNetwork(RegistryFriendlyByteBuf byteBuf) {
			int resultSize = byteBuf.readVarInt();
			NonNullList<Result> results = NonNullList.withSize(resultSize, new Result(ItemStack.EMPTY, ConstantInt.of(1)));
			results.replaceAll(result -> Result.STREAM_CODEC.decode(byteBuf));
			int ingredientSize = byteBuf.readVarInt();
			NonNullList<Ingredient> ingredients = NonNullList.withSize(ingredientSize, Ingredient.of(net.minecraft.world.item.Items.STONE));
			ingredients.replaceAll(ingredient -> Ingredient.CONTENTS_STREAM_CODEC.decode(byteBuf));
			int burnTime = byteBuf.readInt();
			return new AlloyRecipe(results, ingredients, burnTime);
		}

		private static void toNetwork(RegistryFriendlyByteBuf byteBuf, AlloyRecipe alloyRecipe) {
			byteBuf.writeVarInt(alloyRecipe.results.size());
			for (Result result : alloyRecipe.results) {
				Result.STREAM_CODEC.encode(byteBuf, result);
			}
			byteBuf.writeVarInt(alloyRecipe.ingredients.size());
			for (Ingredient ingredient : alloyRecipe.ingredients) {
				Ingredient.CONTENTS_STREAM_CODEC.encode(byteBuf, ingredient);
			}
			byteBuf.writeInt(alloyRecipe.burnTime);
		}
	}
}
