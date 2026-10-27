package cn.leolezury.eternalstarlight.common.item.recipe;

import cn.leolezury.eternalstarlight.common.EternalStarlight;
import cn.leolezury.eternalstarlight.common.registry.ESRecipeSerializers;
import cn.leolezury.eternalstarlight.common.registry.ESRecipes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

public record GeyserSmokingRecipe(Item input, int inputCount, ItemStack output) implements Recipe<SingleRecipeInput> {
	@Override
	public boolean matches(SingleRecipeInput container, Level level) {
		return container.getItem(0).is(input()) && container.getItem(0).getCount() >= inputCount();
	}

	@Override
	public ItemStack assemble(SingleRecipeInput container) {
		return output().copy();
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
	public RecipeSerializer<GeyserSmokingRecipe> getSerializer() {
		return ESRecipeSerializers.GEYSER_SMOKING.get();
	}

	@Override
	public RecipeType<GeyserSmokingRecipe> getType() {
		return ESRecipes.GEYSER_SMOKING.get();
	}

	public static class Type implements RecipeType<GeyserSmokingRecipe> {
		public static final Identifier ID = EternalStarlight.id("geyser_smoking");

		@Override
		public String toString() {
			return ID.toString();
		}
	}

	public static class Serializer {
		public RecipeSerializer<GeyserSmokingRecipe> build() {
			return new RecipeSerializer<>(codec(), streamCodec());
		}

		private static final MapCodec<GeyserSmokingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			BuiltInRegistries.ITEM.byNameCodec().fieldOf("input").forGetter(GeyserSmokingRecipe::input),
			Codec.INT.fieldOf("input_count").forGetter(GeyserSmokingRecipe::inputCount),
			ItemStack.OPTIONAL_CODEC.fieldOf("output").forGetter(GeyserSmokingRecipe::output)
		).apply(instance, GeyserSmokingRecipe::new));

		public MapCodec<GeyserSmokingRecipe> codec() {
			return CODEC;
		}

		public StreamCodec<RegistryFriendlyByteBuf, GeyserSmokingRecipe> streamCodec() {
			return new StreamCodec<>() {
				@Override
				public GeyserSmokingRecipe decode(RegistryFriendlyByteBuf friendlyByteBuf) {
					Item input = friendlyByteBuf.readById(BuiltInRegistries.ITEM::byId);
					int inputCount = friendlyByteBuf.readInt();
					ItemStack output = ItemStack.STREAM_CODEC.decode(friendlyByteBuf);
					return new GeyserSmokingRecipe(input, inputCount, output);
				}

				@Override
				public void encode(RegistryFriendlyByteBuf friendlyByteBuf, GeyserSmokingRecipe recipe) {
					friendlyByteBuf.writeById(BuiltInRegistries.ITEM::getId, recipe.input());
					friendlyByteBuf.writeInt(recipe.inputCount());
					ItemStack.STREAM_CODEC.encode(friendlyByteBuf, recipe.output());
				}
			};
		}
	}
}
