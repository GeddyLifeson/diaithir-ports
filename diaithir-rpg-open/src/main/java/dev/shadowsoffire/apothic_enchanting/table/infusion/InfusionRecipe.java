package dev.shadowsoffire.apothic_enchanting.table.infusion;

import java.util.List;

import javax.annotation.Nullable;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apothic_enchanting.Ench;
import dev.shadowsoffire.apothic_enchanting.table.EnchantingStatRegistry.Stats;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.level.Level;

public class InfusionRecipe implements Recipe<RecipeInput> {

    public static final Stats NO_MAX = new Stats(-1, -1, -1, -1, -1);

    public static final MapCodec<InfusionRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
        ItemStackTemplate.MAP_CODEC.fieldOf("result").forGetter(InfusionRecipe::getOutput),
        Ingredient.CODEC.fieldOf("input").forGetter(InfusionRecipe::getInput),
        Stats.CODEC.fieldOf("requirements").forGetter(InfusionRecipe::getRequirements),
        Stats.CODEC.optionalFieldOf("max_requirements", NO_MAX).forGetter(InfusionRecipe::getMaxRequirements))
        .apply(inst, InfusionRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, InfusionRecipe> STREAM_CODEC = StreamCodec.composite(
        ItemStackTemplate.STREAM_CODEC, InfusionRecipe::getOutput,
        Ingredient.CONTENTS_STREAM_CODEC, InfusionRecipe::getInput,
        Stats.STREAM_CODEC, InfusionRecipe::getRequirements,
        Stats.STREAM_CODEC, InfusionRecipe::getMaxRequirements,
        InfusionRecipe::new);

    public static final RecipeSerializer<InfusionRecipe> SERIALIZER = new RecipeSerializer<>(CODEC, STREAM_CODEC);

    protected final ItemStackTemplate output;
    protected final Ingredient input;
    protected final Stats requirements, maxRequirements;

    /**
     * Defines an Infusion Enchanting recipe.
     *
     * @param output       The output template (converted to an {@link ItemStack} at assemble time)
     * @param input        The input Ingredient
     * @param requirements The Level, Quanta, and Arcana requirements respectively.
     * @param maxRequirements Caps on the three stats above which the recipe stops matching, or -1 for no cap.
     */
    public InfusionRecipe(ItemStackTemplate output, Ingredient input, Stats requirements, Stats maxRequirements) {
        this.output = output;
        this.input = input;
        this.requirements = requirements;
        this.maxRequirements = maxRequirements;

        if (maxRequirements.eterna() != -1 && requirements.eterna() > maxRequirements.eterna()) throw new UnsupportedOperationException("Invalid min/max eterna bounds (min > max).");
        if (maxRequirements.quanta() != -1 && requirements.quanta() > maxRequirements.quanta()) throw new UnsupportedOperationException("Invalid min/max quanta bounds (min > max).");
        if (maxRequirements.arcana() != -1 && requirements.arcana() > maxRequirements.arcana()) throw new UnsupportedOperationException("Invalid min/max arcana bounds (min > max).");
    }

    public boolean matches(ItemStack input, float eterna, float quanta, float arcana) {
        if (this.maxRequirements.eterna() > -1 && eterna > this.maxRequirements.eterna() ||
            this.maxRequirements.quanta() > -1 && quanta > this.maxRequirements.quanta() ||
            this.maxRequirements.arcana() > -1 && arcana > this.maxRequirements.arcana()) {
            return false;
        }
        return this.input.test(input) && eterna >= this.requirements.eterna() && quanta >= this.requirements.quanta() && arcana >= this.requirements.arcana();
    }

    public Stats getRequirements() {
        return this.requirements;
    }

    public Stats getMaxRequirements() {
        return this.maxRequirements;
    }

    public Ingredient getInput() {
        return this.input;
    }

    public ItemStackTemplate getOutput() {
        return this.output;
    }

    public ItemStack assemble(ItemStack input, float eterna, float quanta, float arcana) {
        return this.output.create();
    }

    @Override
    public RecipeSerializer<? extends Recipe<RecipeInput>> getSerializer() {
        return InfusionRecipe.SERIALIZER;
    }

    @Override
    public RecipeType<? extends Recipe<RecipeInput>> getType() {
        return Ench.RecipeTypes.INFUSION;
    }

    @Override
    @Deprecated
    public boolean matches(RecipeInput pContainer, Level pLevel) {
        return false;
    }

    @Override
    @Deprecated
    public ItemStack assemble(RecipeInput pContainer) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean isSpecial() {
        return true;
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
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }

    @Override
    public List<RecipeDisplay> display() {
        return List.of();
    }

    @Nullable
    public static InfusionRecipe findMatch(Level level, ItemStack input, float eterna, float quanta, float arcana) {
        return InfusionRecipeCache.findMatch(input, eterna, quanta, arcana);
    }

    @Nullable
    public static InfusionRecipe findItemMatch(Level level, ItemStack toEnchant) {
        return InfusionRecipeCache.findItemMatch(toEnchant);
    }

}
