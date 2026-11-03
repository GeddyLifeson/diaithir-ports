package org.diaithir.archimedes;

import net.minecraft.world.level.block.state.BlockState;

/** Block density in the original's units: water is 1.0, default block 0.34. */
public final class ShipDensity {
    public static final float WATER = 1.0f;

    private ShipDensity() {}

    public static float of(BlockState state) {
        var b = state.getBlock();
        if (b == ArchimedesMod.FLOATER.get()) return 0.05f;
        if (b instanceof org.diaithir.archimedes.block.BalloonBlock) return 0.02f;
        if (b == ArchimedesMod.ENGINE.get()) return 1.2f;
        float hardness = state.getDestroySpeed(net.minecraft.world.level.EmptyBlockGetter.INSTANCE, net.minecraft.core.BlockPos.ZERO);
        return Math.min(1.4f, 0.2f + Math.max(0f, hardness) * 0.08f);
    }
}
