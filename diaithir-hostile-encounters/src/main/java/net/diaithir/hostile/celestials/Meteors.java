package net.diaithir.hostile.celestials;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;

/** A meteor impact: a bowl in the surface with a magma core and an ore rim. Plain vanilla blocks, no entity and no art. */
public final class Meteors {
    private static final Block[] ORES = {Blocks.IRON_ORE, Blocks.IRON_ORE, Blocks.COPPER_ORE, Blocks.GOLD_ORE, Blocks.REDSTONE_ORE, Blocks.DIAMOND_ORE};

    /** Drops one meteor within reach of a random player. Returns the impact point, or null when no safe place was found. */
    public static BlockPos strike(ServerLevel level, Lunar.Meteor cfg) {
        if (level.players().isEmpty()) return null;
        ServerPlayer p = level.players().get(level.getRandom().nextInt(level.players().size()));
        double a = level.getRandom().nextDouble() * Math.PI * 2;
        double d = cfg.min_distance + level.getRandom().nextDouble() * (cfg.max_distance - cfg.min_distance);
        int x = (int) (p.getX() + Math.cos(a) * d), z = (int) (p.getZ() + Math.sin(a) * d);
        if (!level.hasChunkAt(new BlockPos(x, 0, z))) return null;
        BlockPos top = new BlockPos(x, level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z), z);
        if (!level.getFluidState(top.below()).isEmpty()) return null;
        impact(level, top.below(), cfg.radius);
        return top.below();
    }

    public static void impact(ServerLevel level, BlockPos centre, int r) {
        for (BlockPos pos : BlockPos.betweenClosed(centre.offset(-r, -r, -r), centre.offset(r, r, r))) {
            double dist = Math.sqrt(pos.distSqr(centre));
            if (dist > r + 0.5) continue;
            BlockPos at = pos.immutable();
            if (level.getBlockState(at).is(Blocks.BEDROCK)) continue;
            if (dist <= 1.0 && at.getY() <= centre.getY()) level.setBlock(at, Blocks.MAGMA_BLOCK.defaultBlockState(), Block.UPDATE_ALL);
            else if (at.getY() > centre.getY() - 1) level.setBlock(at, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            else if (dist > r - 1 && level.getRandom().nextInt(3) == 0) level.setBlock(at, ORES[level.getRandom().nextInt(ORES.length)].defaultBlockState(), Block.UPDATE_ALL);
            else level.setBlock(at, Blocks.OBSIDIAN.defaultBlockState(), Block.UPDATE_ALL);
        }
        level.explode(null, centre.getX() + 0.5, centre.getY() + 1.0, centre.getZ() + 0.5, 1.5F, Level.ExplosionInteraction.NONE);
        level.playSound(null, centre, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.AMBIENT, 4.0F, 0.6F);
    }

    private Meteors() {}
}
