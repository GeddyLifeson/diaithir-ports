package cn.leolezury.eternalstarlight.common.item.recipe;

import cn.leolezury.eternalstarlight.common.EternalStarlight;
import cn.leolezury.eternalstarlight.common.registry.ESRecipeSerializers;
import cn.leolezury.eternalstarlight.common.registry.ESRecipes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public record DryingRecipe(Ingredient input, ItemStack output, int durationTicks, boolean fireBelow) implements Recipe<DryingRecipeInput> {
	@Override
	public boolean matches(DryingRecipeInput container, Level level) {
		return input().test(container.input()) && fireBelow() == container.fireBelow();
	}

	@Override
	public ItemStack assemble(DryingRecipeInput container) {
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
	public RecipeSerializer<DryingRecipe> getSerializer() {
		return ESRecipeSerializers.DRYING.get();
	}

	@Override
	public RecipeType<DryingRecipe> getType() {
		return ESRecipes.DRYING.get();
	}

	public static class Type implements RecipeType<DryingRecipe> {
		public static final Identifier ID = EternalStarlight.id("drying");

		@Override
		public String toString() {
			return ID.toString();
		}
	}

	public static class Serializer {
		public RecipeSerializer<DryingRecipe> build() {
			return new RecipeSerializer<>(codec(), streamCodec());
		}

		private static final MapCodec<DryingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			Ingredient.CODEC.fieldOf("input").forGetter(DryingRecipe::input),
			ItemStack.OPTIONAL_CODEC.fieldOf("output").forGetter(DryingRecipe::output),
			Codec.INT.fieldOf("duration_ticks").forGetter(DryingRecipe::durationTicks),
			Codec.BOOL.fieldOf("fire_below").forGetter(DryingRecipe::fireBelow)
		).apply(instance, DryingRecipe::new));

		public MapCodec<DryingRecipe> codec() {
			return CODEC;
		}

		public StreamCodec<RegistryFriendlyByteBuf, DryingRecipe> streamCodec() {
			return new StreamCodec<>() {
				@Override
				public DryingRecipe decode(RegistryFriendlyByteBuf friendlyByteBuf) {
					Ingredient input = Ingredient.CONTENTS_STREAM_CODEC.decode(friendlyByteBuf);
					ItemStack output = ItemStack.STREAM_CODEC.decode(friendlyByteBuf);
					int durationTicks = friendlyByteBuf.readInt();
					boolean fireBelow = friendlyByteBuf.readBoolean();
					return new DryingRecipe(input, output, durationTicks, fireBelow);
				}

				@Override
				public void encode(RegistryFriendlyByteBuf friendlyByteBuf, DryingRecipe recipe) {
					Ingredient.CONTENTS_STREAM_CODEC.encode(friendlyByteBuf, recipe.input());
					ItemStack.STREAM_CODEC.encode(friendlyByteBuf, recipe.output());
					friendlyByteBuf.writeInt(recipe.durationTicks());
					friendlyByteBuf.writeBoolean(recipe.fireBelow());
				}
			};
		}
	}
}
