package net.diaithir.harbour;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/** Placeholder until the harbour office lands; a bollard reaches 3 blocks. */
final class Office {
    private Office() {}

    static int reachNear(Level level, BlockPos pos) {
        return 3;
    }
}
