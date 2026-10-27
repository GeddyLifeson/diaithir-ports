package org.diaithir.connected.content;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface IConnectionForwardingBlock {
    /**
     * Modify the position of the connection to the neighbour block; the connection is assumed to work both ways.
     */
    BlockPos forwardConnection(Level level, BlockPos sourcePos, BlockState sourceState, BlockPos neighborPos);
}
