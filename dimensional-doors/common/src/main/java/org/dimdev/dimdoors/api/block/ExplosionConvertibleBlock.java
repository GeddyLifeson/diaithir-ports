package org.dimdev.dimdoors.api.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public interface ExplosionConvertibleBlock {
    default void onBlockExploded(BlockState state, Level level, BlockPos pos, Explosion explosion) {
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        if (level instanceof net.minecraft.server.level.ServerLevel sl) ((Block) this).wasExploded(sl, pos, explosion);
    }
}
