package com.gregtechceu.gtceu.api.recipe.lookup.ingredient.item;

import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.AbstractMapIngredient;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CustomItemMapIngredient extends AbstractMapIngredient {

    protected ItemStack stack;
    protected Ingredient ingredient = null;

    public CustomItemMapIngredient(ItemStack stack) {
        this.stack = stack;
    }

    public CustomItemMapIngredient(ItemStack stack, Ingredient ingredient) {
        this.stack = stack;
        this.ingredient = ingredient;
    }

    public static List<AbstractMapIngredient> from(Ingredient ingredient) { return null; /* ported stub */ }

    @NotNull
    public static List<AbstractMapIngredient> from(ItemStack stack) {
        return Collections.singletonList(new CustomItemMapIngredient(stack));
    }

    @Override
    public boolean equals(Object o) { return false; /* ported stub */ }

    @Override
    protected int hash() { return (int)0; /* ported stub */ }

    @Override
    public String toString() {
        return "CustomMapIngredient{" +
                "item=" + stack +
                "ingredient=" + ingredient +
                "}";
    }

    @Override
    public boolean isSpecialIngredient() {
        return true;
    }
}
