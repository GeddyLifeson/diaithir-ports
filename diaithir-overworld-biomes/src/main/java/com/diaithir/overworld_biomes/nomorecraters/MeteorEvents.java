package com.diaithir.overworld_biomes.nomorecraters;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.common.NeoForge;

/** Global Events hook point + hint marker placement. */
public final class MeteorEvents {
    private MeteorEvents() {}

    /** Fired (server-side, worldgen thread) once a meteor placement has been resolved. */
    public static final class MeteorBuriedEvent extends Event {
        private final BlockPos origin;
        private final MeteorPlacement.Result result;

        public MeteorBuriedEvent(BlockPos origin, MeteorPlacement.Result result) {
            this.origin = origin;
            this.result = result;
        }

        public BlockPos origin() { return origin; }
        public MeteorPlacement.Result result() { return result; }
    }

    public static void register() {
        // Hook is fired by MeteorHook.apply; nothing to subscribe to on the bus here.
    }

    public static void fire(BlockPos origin, MeteorPlacement.Result result) {
        NeoForge.EVENT_BUS.post(new MeteorBuriedEvent(origin, result));
    }

    public static void placeHint(WorldGenLevel level, BlockPos origin) {
        int y = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE_WG, origin.getX(), origin.getZ());
        BlockPos pos = new BlockPos(origin.getX(), y, origin.getZ());
        if (level.getBlockState(pos).isAir()) {
            level.setBlock(pos, NoMoreCraters.METEOR_TRACE.get().defaultBlockState(), 2);
        }
    }
}
