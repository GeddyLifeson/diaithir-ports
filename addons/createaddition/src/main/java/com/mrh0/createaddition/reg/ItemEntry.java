package com.mrh0.createaddition.reg;

import java.util.function.Supplier;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class ItemEntry<T extends Item> implements Supplier<T>, ItemLike {
    private final T item;
    public ItemEntry(T item) { this.item = item; }
    @Override public T get() { return item; }
    @Override public Item asItem() { return item; }
    public ItemStack asStack() { return new ItemStack(item); }
    public ItemStack asStack(int n) { return new ItemStack(item, n); }
    public boolean is(ItemStack s) { return s.is(item); }
}
