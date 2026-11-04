package com.gregtechceu.gtceu.compat.client;

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

/** Replacement for the removed 1.21 block colour handler; tint sources replace it in 26.1. */
@FunctionalInterface
public interface BlockColor {
    int getColor(BlockState state, @Nullable BlockAndTintGetter level, @Nullable BlockPos pos, int tintIndex);
}
