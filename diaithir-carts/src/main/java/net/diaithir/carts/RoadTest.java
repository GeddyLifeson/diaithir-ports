package net.diaithir.carts;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

/** /diaithircarts roadtest: every road block exists with its table speed factor, and the lookup finds it in a real world (then restores the block). */
final class RoadTest {
    private RoadTest() {}

    /** Returns null on pass, otherwise what failed. */
    static String run(MinecraftServer server) {
        ServerLevel level = server.getLevel(Level.OVERWORLD);
        if (level == null) return "no overworld";
        BlockPos pos = new BlockPos(level.getRespawnData().pos().getX(), 250, level.getRespawnData().pos().getZ());
        level.getChunk(pos);
        var old = level.getBlockState(pos);
        try {
            float last = 0;
            for (Roads r : Roads.ALL) {
                var block = DiaithirCarts.ROAD_BLOCKS.get(r).get();
                if (block.getSpeedFactor() != r.speedFactor) return r.id + " speed factor " + block.getSpeedFactor() + " != " + r.speedFactor;
                if (r.speedFactor < last) return r.id + " is slower than the tier before it";
                last = r.speedFactor;
                level.setBlock(pos, block.defaultBlockState(), 3);
                if (Roads.factorAt(level, pos) != r.speedFactor) return r.id + " lookup failed";
            }
            level.setBlock(pos, Blocks.STONE.defaultBlockState(), 3);
            if (Roads.factorAt(level, pos) != 1.0f) return "stone should be 1.0";
            return null;
        } finally {
            level.setBlock(pos, old, 3);
        }
    }
}
