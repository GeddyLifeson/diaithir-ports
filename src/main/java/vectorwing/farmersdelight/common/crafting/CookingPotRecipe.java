package vectorwing.farmersdelight.common.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.util.RecipeMatcher;
import vectorwing.farmersdelight.client.recipebook.CookingPotRecipeBookTab;
import vectorwing.farmersdelight.common.registry.ModRecipeTypes;
import vectorwing.farmersdelight.common.utility.ItemUtils;

import javax.annotation.Nullable;

public class CookingPotRecipe implements Recipe<RecipeInput>
{
	public static final int INPUT_SLOTS = 6;

	private final String group;
	private final CookingPotRecipeBookTab tab;
	private final NonNullList<Ingredient> inputItems;
	private final ItemStack output;
	private final ItemStack container;
	private final ItemStack containerOverride;
	private final float experience;
	private final int cookTime;
	@Nullable
	private PlacementInfo placementInfo;

	public CookingPotRecipe(String group, @Nullable CookingPotRecipeBookTab tab, NonNullList<Ingredient> inputItems, ItemStack output, ItemStack container, float experience, int cookTime) {
		this.group = group;
		this.tab = tab;
		this.inputItems = inputItems;
		this.output = output;

		if (!container.isEmpty()) {
			this.container = container;
		} else if (!ItemUtils.getCraftingRemainder(output).isEmpty()) {
			this.container = ItemUtils.getCraftingRemainder(output);
		} else {
			this.container = ItemStack.EMPTY;
		}

		this.containerOverride = container;
		this.experience = experience;
		this.cookTime = cookTime;
	}

	@Override
	public String group() {
		return this.group;
	}

	@Nullable
	public CookingPotRecipeBookTab getRecipeBookTab() {
		return this.tab;
	}

	public NonNullList<Ingredient> getIngredients() {
		return this.inputItems;
	}

	public ItemStack getResultItem() {
		return this.output;
	}

	public ItemStack getOutputContainer() {
		return this.container;
	}

	public ItemStack getContainerOverride() {
		return this.containerOverride;
	}

	@Override
	public ItemStack assemble(RecipeInput inv) {
		return this.output.copy();
	}

	public float getExperience() {
		return this.experience;
	}

	public int getCookTime() {
		return this.cookTime;
	}

	@Override
	public boolean matches(RecipeInput inv, Level level) {
		java.util.List<ItemStack> inputs = new java.util.ArrayList<>();
		int i = 0;

		for (int j = 0; j < INPUT_SLOTS; ++j) {
			ItemStack itemstack = inv.getItem(j);
			if (!itemstack.isEmpty()) {
				++i;
				inputs.add(itemstack);
			}
		}
		return i == this.inputItems.size() && RecipeMatcher.findMatches(inputs, this.inputItems) != null;
	}

	@Override
	public boolean showNotification() {
		return true;
	}

	@Override
	public PlacementInfo placementInfo() {
		if (placementInfo == null) {
			placementInfo = PlacementInfo.create(inputItems);
		}
		return placementInfo;
	}

	@Override
	public RecipeBookCategory recipeBookCategory() {
		return ModRecipeTypes.COOKING_BOOK_CATEGORY.get();
	}

	@Override
	public RecipeSerializer<CookingPotRecipe> getSerializer() {
		return SERIALIZER;
	}

	@Override
	public RecipeType<CookingPotRecipe> getType() {
		return ModRecipeTypes.COOKING.get();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		CookingPotRecipe that = (CookingPotRecipe) o;

		if (Float.compare(that.getExperience(), getExperience()) != 0) return false;
		if (getCookTime() != that.getCookTime()) return false;
		if (!group().equals(that.group())) return false;
		if (tab != that.tab) return false;
		if (!inputItems.equals(that.inputItems)) return false;
		if (!ItemStack.matches(output, that.output)) return false;
		return ItemStack.matches(container, that.container);
	}

	@Override
	public int hashCode() {
		int result = group().hashCode();
		result = 31 * result + (getRecipeBookTab() != null ? getRecipeBookTab().hashCode() : 0);
		result = 31 * result + inputItems.hashCode();
		result = 31 * result + ItemStack.hashItemAndComponents(output);
		result = 31 * result + ItemStack.hashItemAndComponents(container);
		result = 31 * result + (getExperience() != 0.0f ? Float.floatToIntBits(getExperience()) : 0);
		result = 31 * result + getCookTime();
		return result;
	}

	private static final MapCodec<CookingPotRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
			Codec.STRING.optionalFieldOf("group", "").forGetter(CookingPotRecipe::group),
			CookingPotRecipeBookTab.CODEC.optionalFieldOf("recipe_book_tab", CookingPotRecipeBookTab.MISC).forGetter(CookingPotRecipe::getRecipeBookTab),
			ExtraCodecs.nonEmptyList(Ingredient.CODEC.listOf()).fieldOf("ingredients").xmap(ingredients -> {
				NonNullList<Ingredient> nonNullList = NonNullList.create();
				nonNullList.addAll(ingredients);
				return nonNullList;
			}, ingredients -> ingredients).forGetter(CookingPotRecipe::getIngredients),
			ItemStack.CODEC.fieldOf("result").forGetter(r -> r.output),
			ItemStack.OPTIONAL_CODEC.optionalFieldOf("container", ItemStack.EMPTY).forGetter(CookingPotRecipe::getContainerOverride),
			Codec.FLOAT.optionalFieldOf("experience", 0.0F).forGetter(CookingPotRecipe::getExperience),
			Codec.INT.optionalFieldOf("cookingtime", 200).forGetter(CookingPotRecipe::getCookTime)
	).apply(inst, CookingPotRecipe::new));

	private static final StreamCodec<RegistryFriendlyByteBuf, CookingPotRecipe> STREAM_CODEC = StreamCodec.of(CookingPotRecipe::toNetwork, CookingPotRecipe::fromNetwork);

	public static final RecipeSerializer<CookingPotRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);

	private static CookingPotRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
		String group = buffer.readUtf();
		CookingPotRecipeBookTab tab = CookingPotRecipeBookTab.findByName(buffer.readUtf());
		int count = buffer.readVarInt();
		NonNullList<Ingredient> inputItems = NonNullList.create();
		for (int i = 0; i < count; i++) {
			inputItems.add(Ingredient.CONTENTS_STREAM_CODEC.decode(buffer));
		}

		ItemStack output = ItemStack.STREAM_CODEC.decode(buffer);
		ItemStack container = ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer);
		float experience = buffer.readFloat();
		int cookTime = buffer.readVarInt();
		return new CookingPotRecipe(group, tab, inputItems, output, container, experience, cookTime);
	}

	private static void toNetwork(RegistryFriendlyByteBuf buffer, CookingPotRecipe recipe) {
		buffer.writeUtf(recipe.group);
		buffer.writeUtf(recipe.tab != null ? recipe.tab.toString() : "");
		buffer.writeVarInt(recipe.inputItems.size());

		for (Ingredient ingredient : recipe.inputItems) {
			Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, ingredient);
		}

		ItemStack.STREAM_CODEC.encode(buffer, recipe.output);
		ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, recipe.container);
		buffer.writeFloat(recipe.experience);
		buffer.writeVarInt(recipe.cookTime);
	}
}
