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

package com.buuz135.industrial.recipe.provider;

import net.minecraft.core.registries.BuiltInRegistries;

import com.buuz135.industrial.module.ModuleCore;
import com.buuz135.industrial.module.ModuleGenerator;
import com.buuz135.industrial.module.ModuleResourceProduction;
import com.buuz135.industrial.recipe.*;
import com.buuz135.industrial.utils.IndustrialTags;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import java.util.List;
import java.util.Optional;

public class IndustrialSerializableProvider {

    public static void init(RecipeOutput output) {
        DissolutionChamberRecipe.createRecipe(output, "pink_slime_ball",
                new DissolutionChamberRecipe(List.of(Ingredient.of(Items.GLASS_PANE)), new FluidStack(ModuleCore.PINK_SLIME.getSourceFluid().get(), 300), 200, Optional.of(new ItemStack(ModuleCore.PINK_SLIME_ITEM.get())), Optional.of(new FluidStack(Fluids.WATER, 150))));
        DissolutionChamberRecipe.createRecipe(output, "pink_slime_ingot",
                new DissolutionChamberRecipe(
                        List.of(
                                Ingredient.of(BuiltInRegistries.ITEM.getOrThrow(Tags.Items.INGOTS_IRON)),
                                Ingredient.of(BuiltInRegistries.ITEM.getOrThrow(Tags.Items.INGOTS_IRON)),
                                Ingredient.of(BuiltInRegistries.ITEM.getOrThrow(Tags.Items.INGOTS_GOLD)),
                                Ingredient.of(BuiltInRegistries.ITEM.getOrThrow(Tags.Items.INGOTS_GOLD))
                        ), new FluidStack(ModuleCore.PINK_SLIME.getSourceFluid().get(), 1000), 300, Optional.of(new ItemStack(ModuleCore.PINK_SLIME_INGOT.get())), Optional.empty()));
        DissolutionChamberRecipe.createRecipe(output, "mechanical_dirt",
                new DissolutionChamberRecipe(
                        List.of(
                                Ingredient.of(Blocks.DIRT),
                                Ingredient.of(Blocks.DIRT),
                                Ingredient.of(Items.ROTTEN_FLESH),
                                Ingredient.of(Items.ROTTEN_FLESH),
                                Ingredient.of(BuiltInRegistries.ITEM.getOrThrow(IndustrialTags.Items.MACHINE_FRAME_PITY))
                        ), new FluidStack(ModuleCore.MEAT.getSourceFluid().get(), 1000), 100, Optional.of(new ItemStack(ModuleResourceProduction.MECHANICAL_DIRT.getBlock())), Optional.empty()));
        DissolutionChamberRecipe.createRecipe(output, "simple_machine_frame", new DissolutionChamberRecipe(
                List.of(
                        Ingredient.of(BuiltInRegistries.ITEM.getOrThrow(IndustrialTags.Items.PLASTIC)),
                        Ingredient.of(BuiltInRegistries.ITEM.getOrThrow(IndustrialTags.Items.MACHINE_FRAME_PITY)),
                        Ingredient.of(BuiltInRegistries.ITEM.getOrThrow(IndustrialTags.Items.PLASTIC)),
                        Ingredient.of(Items.NETHER_BRICK),
                        Ingredient.of(Items.NETHER_BRICK),
                        Ingredient.of(BuiltInRegistries.ITEM.getOrThrow(Tags.Items.INGOTS_IRON)),
                        Ingredient.of(BuiltInRegistries.ITEM.getOrThrow(IndustrialTags.Items.GEAR_GOLD)),
                        Ingredient.of(BuiltInRegistries.ITEM.getOrThrow(Tags.Items.INGOTS_IRON))
                ),
                new FluidStack(ModuleCore.LATEX.getSourceFluid().get(), 250), 300, Optional.of(new ItemStack(ModuleCore.SIMPLE.get())), Optional.empty()));
        DissolutionChamberRecipe.createRecipe(output, "advanced_machine_frame", new DissolutionChamberRecipe(
                List.of(
                        Ingredient.of(BuiltInRegistries.ITEM.getOrThrow(IndustrialTags.Items.PLASTIC)),
                        Ingredient.of(BuiltInRegistries.ITEM.getOrThrow(IndustrialTags.Items.MACHINE_FRAME_SIMPLE)),
                        Ingredient.of(BuiltInRegistries.ITEM.getOrThrow(IndustrialTags.Items.PLASTIC)),
                        Ingredient.of(Items.NETHERITE_SCRAP),
                        Ingredient.of(Items.NETHERITE_SCRAP),
                        Ingredient.of(BuiltInRegistries.ITEM.getOrThrow(Tags.Items.INGOTS_GOLD)),
                        Ingredient.of(BuiltInRegistries.ITEM.getOrThrow(IndustrialTags.Items.GEAR_DIAMOND)),
                        Ingredient.of(BuiltInRegistries.ITEM.getOrThrow(Tags.Items.INGOTS_GOLD))
                ),
                new FluidStack(ModuleCore.PINK_SLIME.getSourceFluid().get(), 500), 300, Optional.of(new ItemStack(ModuleCore.ADVANCED.get())), Optional.empty()));
        DissolutionChamberRecipe.createRecipe(output, "supreme_machine_frame", new DissolutionChamberRecipe(
                List.of(
                        Ingredient.of(BuiltInRegistries.ITEM.getOrThrow(IndustrialTags.Items.PLASTIC)),
                        Ingredient.of(BuiltInRegistries.ITEM.getOrThrow(IndustrialTags.Items.MACHINE_FRAME_ADVANCED)),
                        Ingredient.of(BuiltInRegistries.ITEM.getOrThrow(IndustrialTags.Items.PLASTIC)),
                        Ingredient.of(Items.NETHERITE_INGOT),
                        Ingredient.of(Items.NETHERITE_INGOT),
                        Ingredient.of(BuiltInRegistries.ITEM.getOrThrow(Tags.Items.GEMS_DIAMOND)),
                        Ingredient.of(BuiltInRegistries.ITEM.getOrThrow(IndustrialTags.Items.GEAR_DIAMOND)),
                        Ingredient.of(BuiltInRegistries.ITEM.getOrThrow(Tags.Items.GEMS_DIAMOND))
                ),
                new FluidStack(ModuleCore.ETHER.getSourceFluid().get(), 135), 300, Optional.of(new ItemStack(ModuleCore.SUPREME.get())), Optional.empty()));
        DissolutionChamberRecipe.createRecipe(output, "mycelial_reactor", new DissolutionChamberRecipe(
                List.of(
                        Ingredient.of(BuiltInRegistries.ITEM.getOrThrow(IndustrialTags.Items.PLASTIC)),
                        Ingredient.of(BuiltInRegistries.ITEM.getOrThrow(IndustrialTags.Items.MACHINE_FRAME_SUPREME)),
                        Ingredient.of(BuiltInRegistries.ITEM.getOrThrow(IndustrialTags.Items.PLASTIC)),
                        Ingredient.of(Items.NETHERITE_INGOT),
                        Ingredient.of(Items.NETHERITE_INGOT),
                        Ingredient.of(BuiltInRegistries.ITEM.getOrThrow(IndustrialTags.Items.GEAR_DIAMOND)),
                        Ingredient.of(Items.NETHER_STAR),
                        Ingredient.of(BuiltInRegistries.ITEM.getOrThrow(IndustrialTags.Items.GEAR_DIAMOND))
                ),
                new FluidStack(ModuleCore.ETHER.getSourceFluid().get(), 500), 600, Optional.of(new ItemStack(ModuleGenerator.MYCELIAL_REACTOR.getBlock())), Optional.empty()));
        DissolutionChamberRecipe.createRecipe(output, "xp_bottles", new DissolutionChamberRecipe(
                List.of(),
                SizedFluidIngredient.of(IndustrialTags.Fluids.EXPERIENCE, 250), 5, Optional.of(new ItemStack(Items.EXPERIENCE_BOTTLE)), Optional.empty()));

        FluidExtractorRecipe.init(output);
        LaserDrillFluidRecipe.init(output);
        LaserDrillOreRecipe.init(output);
        StoneWorkGenerateRecipe.init(output);
        CrusherRecipe.init(output);

    }

    /*public void save(RecipeOutput consumerIn, Recipe recipe, Identifier id)
    {
        this.validate(id);

        var advancementBuilder = Advancement.Builder.advancement();
        advancementBuilder
                .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id))
                .rewards(AdvancementRewards.Builder.recipe(id))
                .requirements(AdvancementRequirements.Strategy.OR);
        criteria.forEach(advancementBuilder::addCriterion);
        Identifier advancementId = id.withPrefix("recipes/" + category.getFolderName() + "/" );



        consumerIn.accept(
                id,
                recipe,
                advancementBuilder.build(advancementId));
    }*/
}
