package net.diaithir.linkedstorage;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;

/** Linked Pair mode: two entangled blocks share one channel. The pair frequency is derived from both positions so it is symmetric and unique, and both blocks adopt it as their own. */
public final class Pair {
    private Pair() {}

    /** Symmetric 12-bit frequency for two positions: order does not matter. */
    public static int frequencyFor(GlobalPos a, GlobalPos b) {
        long ha = hash(a);
        long hb = hash(b);
        long lo = Math.min(ha, hb);
        long hi = Math.max(ha, hb);
        long mixed = lo * 0x9E3779B97F4A7C15L ^ Long.rotateLeft(hi, 21) * 0xC2B2AE3D27D4EB4FL;
        return (int) ((mixed ^ (mixed >>> 32)) & (Frequency.CHANNELS - 1));
    }

    private static long hash(GlobalPos p) {
        BlockPos b = p.pos();
        return b.asLong() * 31L + p.dimension().identifier().hashCode();
    }
}
