package com.gregtechceu.gtceu.api.recipe.lookup.ingredient.item;

import com.gregtechceu.gtceu.api.recipe.ingredient.SizedIngredientExtensions;
import com.gregtechceu.gtceu.api.recipe.lookup.ingredient.AbstractMapIngredient;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.experimental.ExtensionMethod;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@ExtensionMethod(SizedIngredientExtensions.class)
public class ItemDataComponentMapIngredient extends ItemStackMapIngredient {

    protected DataComponentIngredient componentIngredient;

    public ItemDataComponentMapIngredient(ItemStack s, DataComponentIngredient ingredient, Ingredient vanilla) {
        super(s, vanilla);
        this.componentIngredient = ingredient;
    }

    @NotNull
    public static List<AbstractMapIngredient> from(@NotNull DataComponentIngredient ingredient) { return null; /* ported stub */ }

    @NotNull
    public static List<AbstractMapIngredient> from(@NotNull ItemStack stack) {
        ObjectArrayList<AbstractMapIngredient> list = new ObjectArrayList<>();

        Ingredient strict = DataComponentIngredient.of(true, stack);
        list.add(new ItemDataComponentMapIngredient(stack,
                (DataComponentIngredient) strict.getCustomIngredient(), strict));

        Ingredient partial = DataComponentIngredient.of(false, stack);
        list.add(new ItemDataComponentMapIngredient(stack,
                (DataComponentIngredient) partial.getCustomIngredient(), partial));

        return list;
    }

    @Override
    public boolean equals(Object obj) { return false; /* ported stub */ }

    @Override
    public String toString() {
        return "DataComponentItemStackMapIngredient{item=" + BuiltInRegistries.ITEM.getKey(stack.getItem()) + "}";
    }

    @Override
    public boolean isSpecialIngredient() {
        return true;
    }
}
