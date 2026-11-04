package net.diaithir.freight;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

/** The wall cell at the low corner of a container; its block entity remembers the type and which way the length runs. */
public final class ContainerCore extends ContainerWall implements EntityBlock {
    public ContainerCore(Properties props) { super(props); }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new ContainerCoreEntity(pos, state); }
}
