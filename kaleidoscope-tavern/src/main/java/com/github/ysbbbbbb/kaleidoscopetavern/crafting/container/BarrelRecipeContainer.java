package com.github.ysbbbbbb.kaleidoscopetavern.crafting.container;

import com.google.common.collect.Lists;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.transfer.fluid.FluidStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemUtil;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class BarrelRecipeContainer implements RecipeInput {
    private final List<ItemStack> items;
    private final Fluid fluid;

    public BarrelRecipeContainer(ItemStacksResourceHandler itemHandler, FluidStacksResourceHandler fluidHandler) {
        this.items = Lists.newArrayList();
        for (int i = 0; i < itemHandler.size(); i++) {
            ItemStack stack = ItemUtil.getStack(itemHandler, i);
            if (stack.isEmpty()) {
                continue;
            }
            this.items.add(stack);
        }
        this.fluid = fluidHandler.getResource(0).getFluid();
    }

    @Override
    @NonNull
    public ItemStack getItem(int index) {
        return items.get(index);
    }

    @Override
    public int size() {
        return items.size();
    }

    /**
     * 需要重写这个方法，把流体算在内，才不会导致流体不为空但物品为空时被误判为输入空了
     */
    @Override
    public boolean isEmpty() {
        return this.fluid.isSame(Fluids.EMPTY);
    }

    public Fluid getFluid() {
        return fluid;
    }

    public List<ItemStack> getItems() {
        return items;
    }

    public boolean itemsIsEmpty() {
        return items.stream().allMatch(ItemStack::isEmpty);
    }
}
