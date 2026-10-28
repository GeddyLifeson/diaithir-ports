package com.gregtechceu.gtceu.api.recipe.ingredient;

import com.gregtechceu.gtceu.data.recipe.GTIngredientTypes;

import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentExactPredicate;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;

import net.neoforged.neoforge.fluids.crafting.DataComponentFluidIngredient;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;
import net.neoforged.neoforge.fluids.crafting.FluidIngredientType;

import com.mojang.serialization.MapCodec;
import org.jetbrains.annotations.NotNull;

/**
 * Data component fluid ingredient.
 *
 * @see DataComponentFluidIngredient DataComponentFluidIngredient, the parent class
 */
public class ExDataComponentFluidIngredient extends DataComponentFluidIngredient {

    public static final MapCodec<ExDataComponentFluidIngredient> CODEC = DataComponentFluidIngredient.CODEC
            .xmap(i -> new ExDataComponentFluidIngredient(i.fluidSet(), i.components(), i.isStrict()), i -> i);

    public ExDataComponentFluidIngredient(HolderSet<Fluid> fluids, DataComponentExactPredicate components,
                                          boolean strict) {
        super(fluids, components, strict);
    }

    @Override
    public @NotNull FluidIngredientType<?> getType() {
        return GTIngredientTypes.DATA_COMPONENT_FLUID_INGREDIENT.get();
    }

    public static @NotNull FluidIngredient of(boolean strict, DataComponentMap map, HolderSet<Fluid> fluids) {
        return new ExDataComponentFluidIngredient(fluids, DataComponentExactPredicate.allOf(map), strict);
    }

    public static @NotNull FluidIngredient of(boolean strict, DataComponentMap map, TagKey<Fluid> tag) {
        return of(strict, map, BuiltInRegistries.FLUID.getOrThrow(tag));
    }
}
