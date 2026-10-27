package org.dimdev.dimdoors.block;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import org.dimdev.dimdoors.DimensionalDoors;

/** Holds the id of the block currently being constructed so that Properties without an id can be patched (26.1 requires ids on Properties). */
public final class BlockIdHolder {
    public static volatile ResourceKey<Block> CURRENT;

    private BlockIdHolder() {}

    public static <T> T with(String name, java.util.function.Supplier<T> supplier) {
        CURRENT = ResourceKey.create(Registries.BLOCK, DimensionalDoors.id(name));
        try {
            return supplier.get();
        } finally {
            CURRENT = null;
        }
    }
}
