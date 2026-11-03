package com.diaithir.overworld_biomes.nomorecraters;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;

/** Entry point called by the AE2 placement adapter (mixin/feature wrapper). Returns the adjusted core or null to keep default. */
public final class MeteorHook {
    private MeteorHook() {}

    /**
     * @return adjusted core position, or null when placement must be cancelled or left default.
     *         Use {@link #lastCancelled()} semantics via the Result for callers needing the distinction.
     */
    public static MeteorPlacement.Result apply(WorldGenLevel level, BlockPos origin, int radius) {
        MeteorPlacement.Result r = MeteorPlacement.solve(level, origin, radius);
        if (r == null) {
            return null;
        }
        MeteorEvents.fire(origin, r);
        if (!r.cancelled() && r.hint()) {
            MeteorEvents.placeHint(level, origin);
        }
        return r;
    }
}
