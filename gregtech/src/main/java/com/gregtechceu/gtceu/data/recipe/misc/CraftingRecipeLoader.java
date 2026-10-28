package com.gregtechceu.gtceu.data.recipe.misc;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.stack.MaterialEntry;
import com.gregtechceu.gtceu.api.recipe.FacadeCoverRecipe;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidContainerIngredient;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.pipelike.duct.DuctPipeType;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import com.gregtechceu.gtceu.data.recipe.VanillaRecipeHelper;

import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.SpecialRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.Tags;

import org.jetbrains.annotations.NotNull;

import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;
import static com.gregtechceu.gtceu.common.data.GTItems.*;
import static com.gregtechceu.gtceu.common.data.GTMaterials.*;

public class CraftingRecipeLoader {

    public static void init(RecipeOutput provider) {  /* ported stub */ }

    private static void addDuctRecipes(@NotNull RecipeOutput provider, @NotNull Material material,
                                       int outputAmount) {
        VanillaRecipeHelper.addShapedRecipe(provider, "small_duct_%s".formatted(material.getName()),
                GTBlocks.DUCT_PIPES[DuctPipeType.SMALL.ordinal()].asStack(outputAmount * 2), "w", "X", "h",
                'X', new MaterialEntry(plate, material));
        VanillaRecipeHelper.addShapedRecipe(provider, "medium_duct_%s".formatted(material.getName()),
                GTBlocks.DUCT_PIPES[DuctPipeType.NORMAL.ordinal()].asStack(outputAmount), " X ", "wXh", " X ",
                'X', new MaterialEntry(plate, material));
        VanillaRecipeHelper.addShapedRecipe(provider, "large_duct_%s".formatted(material.getName()),
                GTBlocks.DUCT_PIPES[DuctPipeType.LARGE.ordinal()].asStack(outputAmount), "XwX", "X X", "XhX",
                'X', new MaterialEntry(plate, material));
        VanillaRecipeHelper.addShapedRecipe(provider, "huge_duct_%s".formatted(material.getName()),
                GTBlocks.DUCT_PIPES[DuctPipeType.HUGE.ordinal()].asStack(outputAmount), "XwX", "X X", "XhX",
                'X', new MaterialEntry(plateDouble, material));
    }

    // private static void registerFacadeRecipe(RecipeOutput provider, Material material, int facadeAmount)
    // {
    // OreIngredient ingredient = new OreIngredient(new MaterialEntry(plate, material).toString());
    // ForgeRegistries.RECIPES.register(new FacadeRecipe(null, ingredient, facadeAmount).setRegistryName("facade_" +
    // material));
    // }
}
