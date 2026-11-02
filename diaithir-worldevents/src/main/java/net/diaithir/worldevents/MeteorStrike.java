package net.diaithir.worldevents;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.List;

/** A meteor lands near a random player, leaves a crater and seeds it with rare ore. */
final class MeteorStrike implements WorldEvent {
    private static final int RADIUS = 6;
    private static final Block[] ORES = {Blocks.IRON_ORE, Blocks.GOLD_ORE, Blocks.DIAMOND_ORE, Blocks.ANCIENT_DEBRIS};

    @Override public String id() { return "meteor"; }
    @Override public String title() { return "Meteor Strike"; }
    @Override public int weight() { return 10; }
    @Override public boolean possible(ServerLevel level) { return !level.players().isEmpty(); }

    @Override
    public String trigger(ServerLevel level, List<ServerPlayer> players) {
        if (players.isEmpty()) return "No player in the overworld.";
        ServerPlayer target = players.get(WorldEvents.RANDOM.nextInt(players.size()));
        int x = target.blockPosition().getX() + WorldEvents.RANDOM.nextInt(-64, 65);
        int z = target.blockPosition().getZ() + WorldEvents.RANDOM.nextInt(-64, 65);
        BlockPos centre = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, new BlockPos(x, 0, z)).below(2);
        crater(level, centre);
        int ores = seedOre(level, centre);
        level.playSound(null, centre, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.AMBIENT, 6.0F, 0.6F);
        level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, centre.getX(), centre.getY() + 1, centre.getZ(), 3, 2, 2, 2, 0.1);
        WorldEvents.announce(level.getServer(), "A meteor strikes near " + target.getName().getString() + " at "
                + centre.getX() + " " + centre.getY() + " " + centre.getZ() + ".", ChatFormatting.GOLD);
        return "meteor at " + centre.toShortString() + ", " + ores + " ores";
    }

    private static void crater(ServerLevel level, BlockPos c) {
        for (BlockPos p : BlockPos.betweenClosed(c.offset(-RADIUS, -RADIUS, -RADIUS), c.offset(RADIUS, RADIUS, RADIUS))) {
            double d = Math.sqrt(p.distSqr(c));
            BlockState s = level.getBlockState(p);
            if (s.is(Blocks.BEDROCK) || d > RADIUS) continue;
            if (d <= RADIUS - 1.5) level.setBlock(p, Blocks.AIR.defaultBlockState(), 3);
            else if (!s.isAir()) level.setBlock(p, (WorldEvents.RANDOM.nextBoolean() ? Blocks.MAGMA_BLOCK : Blocks.BLACKSTONE).defaultBlockState(), 3);
        }
    }

    private static int seedOre(ServerLevel level, BlockPos c) {
        int placed = 0;
        for (int i = 0; i < 40; i++) {
            BlockPos p = c.offset(WorldEvents.RANDOM.nextInt(-8, 9), WorldEvents.RANDOM.nextInt(-8, 3), WorldEvents.RANDOM.nextInt(-8, 9));
            BlockState s = level.getBlockState(p);
            if (!s.is(Blocks.BLACKSTONE) && !s.is(Blocks.MAGMA_BLOCK) && !s.is(Blocks.STONE) && !s.is(Blocks.DIRT)) continue;
            int roll = WorldEvents.RANDOM.nextInt(100);
            Block ore = roll < 45 ? ORES[0] : roll < 75 ? ORES[1] : roll < 95 ? ORES[2] : ORES[3];
            level.setBlock(p, ore.defaultBlockState(), 3);
            if (++placed >= 12) break;
        }
        return placed;
    }
}
