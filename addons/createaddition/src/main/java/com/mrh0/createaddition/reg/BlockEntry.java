package com.mrh0.createaddition.reg;

import java.util.function.Supplier;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/** Registered block with the accessors the Registrate entries used to offer. */
public class BlockEntry<T extends Block> implements Supplier<T> {
    private final T block;
    public BlockEntry(T block) { this.block = block; }
    @Override public T get() { return block; }
    public boolean has(BlockState state) { return state.is(block); }
    public BlockState getDefaultState() { return block.defaultBlockState(); }
    public Item asItem() { return block.asItem(); }
    public ItemStack asStack() { return new ItemStack(block); }
}
