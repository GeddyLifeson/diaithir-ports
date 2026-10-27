package com.diaithir.addons.newage.util;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;

public class RunnableUtil {
    public static Runnable createBlockEntityUpdater(BlockEntity blockEntity) {
        return () -> {
            blockEntity.setChanged();
            if (blockEntity.getLevel() instanceof ServerLevel serverLevel)
                serverLevel.getChunkSource().blockChanged(blockEntity.getBlockPos());
        };
    }
}
