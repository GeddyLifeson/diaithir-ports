package com.gregtechceu.gtceu.api.recipe.ingredient;

import com.gregtechceu.gtceu.data.recipe.GTIngredientTypes;

import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;

import net.neoforged.neoforge.common.crafting.DataComponentIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;

import com.mojang.serialization.MapCodec;
import org.jetbrains.annotations.NotNull;

/**
 * Data component ingredient (the cache-clearing behaviour of the 1.21 version is not needed with HolderSets).
 *
 * @see DataComponentIngredient DataComponentIngredient, the parent class
 */
public class ExDataComponentIngredient extends DataComponentIngredient {

    public static final MapCodec<ExDataComponentIngredient> CODEC = DataComponentIngredient.CODEC
            .xmap(i -> new ExDataComponentIngredient(i.itemSet(), i.components(), i.componentsExhaustive()), i -> i);

    public ExDataComponentIngredient(HolderSet<Item> items, DataComponentPatch components, boolean strict) {
        super(items, components, strict);
    }

    @Override
    public @NotNull IngredientType<?> getType() {
        return GTIngredientTypes.DATA_COMPONENT_INGREDIENT.get();
    }

    public static DataComponentPatch toPatch(DataComponentMap map) {
        var b = DataComponentPatch.builder();
        for (TypedDataComponent<?> c : map) b.set(c);
        return b.build();
    }

    public static @NotNull Ingredient of(boolean strict, DataComponentMap map, HolderSet<Item> items) {
        return new ExDataComponentIngredient(items, toPatch(map), strict).toVanilla();
    }

    public static @NotNull Ingredient of(boolean strict, DataComponentMap map, TagKey<Item> tag) {
        return of(strict, map, BuiltInRegistries.ITEM.getOrThrow(tag));
    }
}
