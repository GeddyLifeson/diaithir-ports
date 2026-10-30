package org.diaithir.connected.content;

import com.zurrtum.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Objects;

public class KineticHelper {
    /** Drops the block entity out of its network and has it reconnect from scratch. */
    public static void updateKineticBlock(KineticBlockEntity be) {
        if (be.hasNetwork()) be.getOrCreateNetwork().remove(be);
        be.detachKinetics();
        be.removeSource();
        BlockState state = be.getBlockState();
        BlockPos pos = be.getBlockPos();
        Level level = Objects.requireNonNull(be.getLevel());
        level.markAndNotifyBlock(pos, level.getChunkAt(pos), state, state, 3, 512);
        if (be instanceof GeneratingKineticBlockEntity gen) gen.reActivateSource = true;
    }
}
