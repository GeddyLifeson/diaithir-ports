package net.diaithir.computing;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/** The stationary computer. */
public final class ReckonerEntity extends ComputerEntity {
    public ReckonerEntity(BlockPos pos, BlockState state) { super(Registry.RECKONER_ENTITY.get(), pos, state); }

    public static void tick(net.minecraft.world.level.Level level, BlockPos pos, BlockState state, ReckonerEntity be) {
        if (!level.isClientSide()) be.tickProgram();
    }
}
