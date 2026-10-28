package com.gregtechceu.gtceu.api.recipe.ingredient;

import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

public class SizedIngredientExtensions {

    public static SizedFluidIngredient shrink(SizedFluidIngredient self, int amount) {
        return new SizedFluidIngredient(self.ingredient(), self.amount() - amount);
    }

    public static SizedFluidIngredient copy(SizedFluidIngredient self) {
        FluidIngredient ingredient = self.ingredient();
        if (ingredient instanceof IntProviderFluidIngredient intProv) {
            ingredient = intProv.copy();
        }
        return new SizedFluidIngredient(ingredient, self.amount());
    }

    public static SizedFluidIngredient copyWithAmount(SizedFluidIngredient self, int amount) {
        return new SizedFluidIngredient(self.ingredient(), amount);
    }

    public static @Nullable ICustomIngredient getContainedCustom(SizedIngredient self) {
        return self.ingredient().getCustomIngredient();
    }

    public static SizedIngredient shrink(SizedIngredient self, int count) {
        return new SizedIngredient(self.ingredient(), self.count() - count);
    }

    public static SizedIngredient copy(SizedIngredient self) {
        if (getContainedCustom(self) instanceof IntCircuitIngredient) {
            return new SizedIngredient(self.ingredient(), self.count());
        } else if (getContainedCustom(self) instanceof IntProviderIngredient intProviderIngredient) {
            return new SizedIngredient(copyIntProvider(intProviderIngredient), self.count());
        }
        return new SizedIngredient(self.ingredient(), self.count());
    }

    public static SizedIngredient copyWithCount(SizedIngredient self, int count) {
        if (self.ingredient().getCustomIngredient() instanceof IntCircuitIngredient) {
            return new SizedIngredient(self.ingredient(), count);
        } else if (self.ingredient().getCustomIngredient() instanceof IntProviderIngredient intProviderIngredient) {
            return new SizedIngredient(copyIntProvider(intProviderIngredient), count);
        }
        return new SizedIngredient(self.ingredient(), count);
    }

    private static Ingredient copyIntProvider(IntProviderIngredient toCopy) {
        var copied = new IntProviderIngredient(toCopy.inner, toCopy.countProvider);
        copied.itemStacks = toCopy.itemStacks;
        copied.sampledCount = toCopy.sampledCount;
        return copied.toVanilla();
    }

    // ---- 26.1 replacements for methods removed from vanilla / NeoForge ingredient classes ----

    public static ItemStack[] getItems(Ingredient self) {
        return self.items().map(ItemStack::new).toArray(ItemStack[]::new);
    }

    public static ItemStack[] getItems(SizedIngredient self) {
        int count = self.count();
        return self.ingredient().items().map(h -> new ItemStack(h, count)).toArray(ItemStack[]::new);
    }

    public static boolean hasNoItems(Ingredient self) {
        return self.items().findAny().isEmpty();
    }

    public static boolean hasNoItems(SizedIngredient self) {
        return hasNoItems(self.ingredient());
    }

    public static FluidStack[] getStacks(FluidIngredient self) {
        return self.fluids().stream().map(h -> new FluidStack(h, 1000)).toArray(FluidStack[]::new);
    }

    public static FluidStack[] getFluids(SizedFluidIngredient self) {
        int amount = self.amount();
        return self.ingredient().fluids().stream().map(h -> new FluidStack(h, amount)).toArray(FluidStack[]::new);
    }

    public static boolean hasNoFluids(FluidIngredient self) {
        return self.fluids().isEmpty();
    }

    public static boolean hasNoFluids(SizedFluidIngredient self) {
        return hasNoFluids(self.ingredient());
    }

    public static boolean isEmpty(FluidIngredient self) {
        return hasNoFluids(self);
    }
}
