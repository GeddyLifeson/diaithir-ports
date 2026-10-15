/*
 * This file is part of Industrial Foregoing.
 *
 * Copyright 2021, Buuz135
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in the
 * Software without restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to the
 * following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies
 * or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.buuz135.industrial.recipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;

import com.buuz135.industrial.module.ModuleCore;
import com.buuz135.industrial.recipe.data.EntityData;
import com.buuz135.industrial.recipe.data.EntityIngredient;
import com.buuz135.industrial.utils.Reference;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.criterion.RecipeUnlockedTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.conditions.ModLoadedCondition;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LaserDrillFluidRecipe extends DrillRecipe<SizedFluidIngredient> {

    public static final MapCodec<LaserDrillFluidRecipe> CODEC = RecordCodecBuilder.mapCodec(in -> in.group(
            SizedFluidIngredient.CODEC.fieldOf("output").forGetter(o -> o.output),
            Ingredient.CODEC.fieldOf("catalyst").forGetter(o -> o.catalyst),
            EntityData.CODEC.optionalFieldOf("entity_data").forGetter(o -> o.entityData),
            LaserDrillRarity.CODEC.listOf().fieldOf("rarity").forGetter(o -> o.rarity)
    ).apply(in, LaserDrillFluidRecipe::new));
    
    public LaserDrillFluidRecipe(SizedFluidIngredient output, Ingredient catalyst, Optional<EntityData> entityData, List<LaserDrillRarity> rarity) {
        super(output, catalyst, entityData, rarity);
    }

    public LaserDrillFluidRecipe(SizedFluidIngredient output, int color, Optional<EntityData> entityData, LaserDrillRarity... rarity) {
        super(output, color, entityData, rarity);
    }
    
    public LaserDrillFluidRecipe(SizedFluidIngredient output, int color, LaserDrillRarity... rarity) {
        super(output, color, Optional.empty(), rarity);
    }


    public static void init(RecipeOutput output) {
        createRecipe(output, "lava", "minecraft", new LaserDrillFluidRecipe(new SizedFluidIngredient(FluidIngredient.of(Fluids.LAVA), 100), 1,
                new LaserDrillRarity(
                        new LaserDrillRarity.BiomeRarity(new ArrayList<>(), new ArrayList<>()),
                        new LaserDrillRarity.DimensionRarity(List.of(BuiltinDimensionTypes.NETHER), new ArrayList<>()),
                        5, 20, 8)));
        createRecipe(output, "ether", "minecraft", new LaserDrillFluidRecipe(new SizedFluidIngredient(FluidIngredient.of(ModuleCore.ETHER.getSourceFluid().get()), 10), 10,
                Optional.of(EntityData.of(EntityType.WITHER)),
                new LaserDrillRarity(
                        new LaserDrillRarity.BiomeRarity(new ArrayList<>(), new ArrayList<>()),
                        new LaserDrillRarity.DimensionRarity(new ArrayList<>(), new ArrayList<>()),
                        -64, 256, 8)));
        if (ModList.get().isLoaded("pneumaticcraft")) {
            createRecipe(output, "oil", "pneumaticcraft",
                    new LaserDrillFluidRecipe(new SizedFluidIngredient(FluidIngredient.of(BuiltInRegistries.FLUID.getValue(Identifier.fromNamespaceAndPath("pneumaticcraft", "oil"))), 50), 15,
                            new LaserDrillRarity(
                                    new LaserDrillRarity.BiomeRarity(LaserDrillRarity.BiomeRarity.OIL, new ArrayList<>()),
                                    new LaserDrillRarity.DimensionRarity(new ArrayList<>(), new ArrayList<>()),
                                    20, 60, 8)));
        }
    }

    public static void createRecipe(RecipeOutput recipeOutput, String name, String modIdCondition, LaserDrillFluidRecipe recipe) {
        var rl = generateRL(name);
        var advancementHolder = recipeOutput.advancement()
                .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(ResourceKey.create(Registries.RECIPE, rl)))
                .rewards(AdvancementRewards.Builder.recipe(ResourceKey.create(Registries.RECIPE, rl)))
                .requirements(AdvancementRequirements.Strategy.OR).build(rl);
        recipeOutput.accept(ResourceKey.create(Registries.RECIPE, rl), recipe, advancementHolder, new ModLoadedCondition(modIdCondition));
    }

    public static Identifier generateRL(String key) {
        return Identifier.fromNamespaceAndPath(Reference.MOD_ID, "laser_drill_fluid/" + key);
    }

    @Override
    @SuppressWarnings("unchecked")
    public RecipeSerializer<LaserDrillFluidRecipe> getSerializer() {
        return (RecipeSerializer<LaserDrillFluidRecipe>) (RecipeSerializer<?>) ModuleCore.LASER_DRILL_FLUID_SERIALIZER.get();
    }

    @Override
    @SuppressWarnings("unchecked")
    public RecipeType<LaserDrillFluidRecipe> getType() {
        return (RecipeType<LaserDrillFluidRecipe>) (RecipeType<?>) ModuleCore.LASER_DRILL_FLUID_TYPE.get();
    }


    public static CompoundTag createNBT(String name, int amount) {
        CompoundTag nbt = new CompoundTag();
        nbt.putString("FluidName", name);
        nbt.putInt("Amount", amount);
        return nbt;
    }
}
