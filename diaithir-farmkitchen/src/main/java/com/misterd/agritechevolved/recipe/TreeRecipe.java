package com.misterd.agritechevolved.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

import java.util.List;

public class TreeRecipe implements Recipe<SingleRecipeInput> {

    private final Ingredient sapling;
    private final List<Ingredient> soils;
    private final List<DropEntry> drops;

    public TreeRecipe(Ingredient sapling, List<Ingredient> soils, List<DropEntry> drops) {
        this.sapling = sapling;
        this.soils = soils;
        this.drops = drops;
    }

    public Ingredient getSapling() { return sapling; }
    public List<Ingredient> getSoils() { return soils; }
    public List<DropEntry> getDrops() { return drops; }

    public boolean matchesSapling(ItemStack stack) {
        return sapling.test(stack);
    }

    public boolean matchesSoil(ItemStack stack) {
        for (Ingredient soil : soils) {
            if (soil.test(stack)) return true;
        }
        return false;
    }

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        return sapling.test(input.item());
    }

    @Override
    public ItemStack assemble(SingleRecipeInput input) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public String group() {
        return "";
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.create(sapling);
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return ATERecipeTypes.TREE_RECIPE_BOOK_CATEGORY;
    }

    @Override
    public RecipeSerializer<TreeRecipe> getSerializer() {
        return ATERecipeTypes.TREE_SERIALIZER.get();
    }

    @Override
    public RecipeType<TreeRecipe> getType() {
        return ATERecipeTypes.TREE_TYPE.get();
    }

    public static final MapCodec<TreeRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Ingredient.CODEC.fieldOf("sapling").forGetter(TreeRecipe::getSapling),
            Ingredient.CODEC.listOf().fieldOf("soils").forGetter(TreeRecipe::getSoils),
            DropEntry.CODEC.listOf().fieldOf("drops").forGetter(TreeRecipe::getDrops)
    ).apply(instance, TreeRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, TreeRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, TreeRecipe::getSapling,
            Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()), TreeRecipe::getSoils,
            DropEntry.STREAM_CODEC.apply(ByteBufCodecs.list()), TreeRecipe::getDrops,
            TreeRecipe::new
    );
}