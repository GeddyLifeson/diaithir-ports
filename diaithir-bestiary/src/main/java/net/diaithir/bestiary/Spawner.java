package net.diaithir.bestiary;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Map;

/**
 * Natural spawns for creatures with a spawn entry. Every 10 seconds each player gets one try at a spot 24 to 48 blocks away:
 * the creatures whose biome tag and light limit fit that spot are summed by weight, and that sum is the percent chance that one
 * of them (picked by weight) spawns there as a group. A creature with a min_age waits until the world's Age reaches it.
 */
final class Spawner {
    static final int INTERVAL = 200, CAP = 6;
    /** A try succeeds with total points out of POINTS: a normal weight is that many percent, a rare one a twentieth of that. */
    static final int POINTS = 2000;

    static int points(CreatureDef.Spawn spawn) {
        return spawn.rare() ? spawn.weight() : spawn.weight() * 20;
    }

    private Spawner() {}

    static void tick(ServerLevel level) {
        if (level.getGameTime() % INTERVAL != 0 || !level.getGameRules().get(GameRules.SPAWN_MOBS)) return;
        Registry<CreatureDef> creatures = DiaithirBestiary.creatures(level);
        RandomSource random = level.getRandom();
        int age = Ages.current(level.getServer());
        for (ServerPlayer player : level.players()) {
            if (player.isSpectator() || level.getEntitiesOfClass(Creature.class, player.getBoundingBox().inflate(64)).size() >= CAP) continue;
            float angle = random.nextFloat() * Mth.TWO_PI;
            int dist = 24 + random.nextInt(25);
            BlockPos column = BlockPos.containing(player.getX() + Mth.cos(angle) * dist, 0, player.getZ() + Mth.sin(angle) * dist);
            if (!level.hasChunkAt(column)) continue;
            BlockPos pos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, column);
            if (!level.getFluidState(pos.below()).isEmpty()) continue;
            List<Map.Entry<Identifier, CreatureDef>> fits = candidates(creatures, level.getBiome(pos), level.getMaxLocalRawBrightness(pos),
                    level.getDifficulty() == Difficulty.PEACEFUL, age);
            int total = fits.stream().mapToInt(e -> points(e.getValue().spawn().get())).sum();
            if (total == 0 || random.nextInt(POINTS) >= total) continue;
            int roll = random.nextInt(total);
            for (Map.Entry<Identifier, CreatureDef> fit : fits) {
                CreatureDef.Spawn spawn = fit.getValue().spawn().get();
                if ((roll -= points(spawn)) >= 0) continue;
                int count = Mth.randomBetweenInclusive(random, spawn.minGroup(), Math.max(spawn.minGroup(), spawn.maxGroup()));
                for (int i = 0; i < count; i++)
                    DiaithirBestiary.spawn(level, fit.getKey(), Vec3.atBottomCenterOf(pos).add(random.nextInt(5) - 2, 0, random.nextInt(5) - 2), EntitySpawnReason.NATURAL);
                break;
            }
        }
    }

    static List<Map.Entry<Identifier, CreatureDef>> candidates(Registry<CreatureDef> creatures, Holder<Biome> biome, int light, boolean peaceful, int age) {
        return creatures.entrySet().stream()
                .filter(e -> e.getValue().spawn().filter(s -> biome.is(s.biomes()) && light <= s.maxLight() && age >= s.minAge()).isPresent())
                .filter(e -> !peaceful || !e.getValue().isHostile())
                .map(e -> Map.entry(e.getKey().identifier(), e.getValue()))
                .toList();
    }
}
