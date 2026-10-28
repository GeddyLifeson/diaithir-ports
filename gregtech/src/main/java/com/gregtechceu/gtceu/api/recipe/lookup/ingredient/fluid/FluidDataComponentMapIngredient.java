package com.gregtechceu.gtceu.api.recipe.lookup.ingredient.fluid;

import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.AbstractMapIngredient;

import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.DataComponentFluidIngredient;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class FluidDataComponentMapIngredient extends FluidStackMapIngredient {

    protected DataComponentFluidIngredient componentIngredient;

    public FluidDataComponentMapIngredient(FluidStack s, DataComponentFluidIngredient componentIngredient) {
        super(s.getFluidHolder());
        this.componentIngredient = componentIngredient;
    }

    @NotNull
    public static List<AbstractMapIngredient> from(@NotNull DataComponentFluidIngredient r) { return null; /* ported stub */ }

    @NotNull
    public static List<AbstractMapIngredient> from(@NotNull FluidStack stack) {
        ObjectArrayList<AbstractMapIngredient> list = new ObjectArrayList<>();

        FluidIngredient strict = DataComponentFluidIngredient.of(true, stack);
        list.add(new FluidDataComponentMapIngredient(stack, (DataComponentFluidIngredient) strict));

        FluidIngredient partial = DataComponentFluidIngredient.of(false, stack);
        list.add(new FluidDataComponentMapIngredient(stack, (DataComponentFluidIngredient) partial));

        return list;
    }

    @Override
    public boolean equals(Object obj) { return false; /* ported stub */ }

    @Override
    public String toString() {
        return "MapFluidStackDataComponentIngredient{fluid=" + BuiltInRegistries.FLUID.getKey(stack.getFluid()) + "}";
    }

    @Override
    public boolean isSpecialIngredient() {
        return true;
    }

    protected int hash() {
        return componentIngredient.hashCode();
    }
}
