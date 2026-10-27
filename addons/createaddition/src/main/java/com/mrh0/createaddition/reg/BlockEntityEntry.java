package com.mrh0.createaddition.reg;

import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class BlockEntityEntry<T extends BlockEntity> implements Supplier<BlockEntityType<T>> {
    private final BlockEntityType<T> type;
    public BlockEntityEntry(BlockEntityType<T> type) { this.type = type; }
    @Override public BlockEntityType<T> get() { return type; }
    public T create(BlockPos pos, BlockState state) { return type.create(pos, state); }
}
