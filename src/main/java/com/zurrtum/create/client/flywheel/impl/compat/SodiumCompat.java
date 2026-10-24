package com.zurrtum.create.client.flywheel.impl.compat;

import com.zurrtum.create.client.flywheel.api.visualization.BlockEntityVisualizer;
import net.minecraft.world.level.block.entity.BlockEntityType;

/** Sodium integration is not part of this build. */
public final class SodiumCompat {
    public static final boolean ACTIVE = false;

    private SodiumCompat() {
    }

    public static <T extends net.minecraft.world.level.block.entity.BlockEntity> Object onSetBlockEntityVisualizer(
        BlockEntityType<T> type,
        BlockEntityVisualizer<? super T> oldVisualizer,
        BlockEntityVisualizer<? super T> newVisualizer,
        Object predicate
    ) {
        return predicate;
    }
}
