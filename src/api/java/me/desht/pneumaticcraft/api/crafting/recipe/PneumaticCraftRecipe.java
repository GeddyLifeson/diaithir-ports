/*
 * This file is part of pnc-repressurized API.
 *
 *     pnc-repressurized API is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     pnc-repressurized is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with pnc-repressurized API.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.desht.pneumaticcraft.api.crafting.recipe;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Base class for all PneumaticCraft machine recipes, which are registered in the vanilla RecipeManager.
 */
public abstract class PneumaticCraftRecipe implements Recipe<CraftingInput> {
    protected PneumaticCraftRecipe() {
    }

    /** 26.1 helpers: the (display) item stacks which an ingredient matches */
    public static java.util.List<ItemStack> itemStacks(net.minecraft.world.item.crafting.Ingredient ingredient) {
        return ingredient.items().map(h -> new ItemStack(h.value())).toList();
    }

    public static java.util.List<ItemStack> itemStacks(net.neoforged.neoforge.common.crafting.SizedIngredient sized) {
        return sized.ingredient().items().map(h -> new ItemStack(h.value(), sized.count())).toList();
    }

    public static java.util.List<net.neoforged.neoforge.fluids.FluidStack> fluidStacks(net.neoforged.neoforge.fluids.crafting.FluidIngredient ingredient, int amount) {
        return ingredient.fluids().stream().map(h -> new net.neoforged.neoforge.fluids.FluidStack(h.value(), amount)).toList();
    }

    public static java.util.List<net.neoforged.neoforge.fluids.FluidStack> fluidStacks(net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient sized) {
        return fluidStacks(sized.ingredient(), sized.amount());
    }

    @Override
    public boolean matches(CraftingInput container, Level level) {
        return true;
    }

    @Override
    public ItemStack assemble(CraftingInput container) {
        return ItemStack.EMPTY;
    }

    public ItemStack getResultItem(HolderLookup.Provider registryAccess) {
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

}
