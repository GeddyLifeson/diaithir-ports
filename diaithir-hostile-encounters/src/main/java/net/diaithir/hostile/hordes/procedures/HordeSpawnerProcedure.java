package net.diaithir.hostile.hordes.procedures;

import net.diaithir.hostile.hordes.HordeHoardConstants;
import net.diaithir.hostile.hordes.init.HordeHoardModGameRules;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Midnight horde-spawner mechanic. Not in the 1.20.1 source — added per user request.
 *
 * Behavior:
 *   - Runs every server tick from the per-loader ServerTick event subscriber.
 *   - When the overworld day-time hits midnight (18000), rolls a 1-in-N chance
 *     (N from the {@code horde_spawner_chance} gamerule).
 *   - On hit, picks a random online overworld player and spawns 100 mobs in a ring
 *     30–60 blocks away. Mix is 70% zombies, 10% husks, 10% drowned, 10% zombie
 *     villagers; ~20% of zombie variants are babies.
 *   - Tracks the last spawn day to avoid multiple rolls within the same midnight tick
 *     window (overworld day-time can dwell at exact-18000 for one tick but the
 *     guard is still cheap insurance).
 *
 * Gamerule semantics:
 *   - 0  → disabled
 *   - 1  → every night
 *   - N  → 1-in-N nights
 *   default 20 (≈ once every Minecraft "month").
 *
 * Manual trigger via {@code /horde [count]} — see HordeHoardCommands.
 */
public class HordeSpawnerProcedure {

    /** Default horde size when spawned automatically at midnight or via /horde with no arg. */
    public static final int DEFAULT_HORDE_SIZE = 100;

    /**
     * Last day-index on which a spawn attempt was made. Single-server scope is fine
     * because horde-spawn is a global single-decision-per-night mechanic.
     */
    private static long lastAttemptDay = -1L;

    /**
     * Diagnostic: last game-time at which we logged a "tick alive" line. Used to gate
     * the diagnostic log to once per ~10 in-game minutes (12000 ticks) so we can
     * verify the per-loader server-tick wiring is actually firing without spamming.
     */
    private static long lastDiagnosticTick = Long.MIN_VALUE;

    public static void onServerTick(MinecraftServer server) {
        for (ServerLevel level : server.getAllLevels()) {
            tickLevel(level);
        }
    }

    private static void tickLevel(ServerLevel level) {
        // Overworld only — nether/end have no day-night cycle that would honor a
        // "midnight" trigger meaningfully. Use .equals() not ==; ResourceKey identity
        // isn't guaranteed across classloaders/dimensions of all loaders.
        if (!level.dimension().equals(Level.OVERWORLD)) {
            return;
        }

        // Diagnostic heartbeat so we can confirm the per-loader server-tick wiring is
        // actually dispatching to this method (suspected as the cause of "horde not
        // spawning on Fabric"). Fires once per ~10 in-game minutes per overworld.
        long gameTime = level.getGameTime();
        if (gameTime - lastDiagnosticTick >= 12000L) {
            lastDiagnosticTick = gameTime;
            HordeHoardConstants.LOGGER.info(
                    "[horde_spawner] heartbeat — game-time {}, day-time {}, day {}, players {}",
                    gameTime,
                    ((level.getOverworldClockTime() % 24000L) + 24000L) % 24000L,
                    Math.floorDiv(level.getOverworldClockTime(), 24000L),
                    level.players().size()
            );
        }

        long dayTime = level.getOverworldClockTime();
        long timeOfDay = ((dayTime % 24000L) + 24000L) % 24000L;
        long currentDay = Math.floorDiv(dayTime, 24000L);

        if (timeOfDay != 18000L) {
            return;
        }
        if (currentDay == lastAttemptDay) {
            return;
        }
        lastAttemptDay = currentDay;

        int chance = level.getGameRules().get(HordeHoardModGameRules.HORDE_SPAWNER_CHANCE);
        HordeHoardConstants.LOGGER.info(
                "[horde_spawner] Midnight reached on day {} (chance gamerule = {})",
                currentDay, chance
        );
        if (chance <= 0) {
            return;
        }
        RandomSource random = level.getRandom();
        if (chance > 1 && random.nextInt(chance) != 0) {
            HordeHoardConstants.LOGGER.info("[horde_spawner] Roll missed, no spawn this night");
            return;
        }

        List<ServerPlayer> players = level.players();
        if (players.isEmpty()) {
            return;
        }
        ServerPlayer target = players.get(random.nextInt(players.size()));

        // Natural midnight spawn uses clump shape (per user preference) but with a
        // random ambush direction rather than the player's look direction — at
        // midnight the player isn't aiming at anything, so "from somewhere in the
        // dark" reads better than "centered on the chest you happen to be facing."
        spawnHordeClumpAmbush(level, target, DEFAULT_HORDE_SIZE);
    }

    /**
     * Spawns a horde of {@code count} mobs in a 30–60 block ring around {@code target}.
     * Returns the number of mobs that successfully spawned (some may be skipped due
     * to height-mismatch or chunk-load issues).
     *
     * Public so {@link net.diaithir.hostile.hordes.command.HordeHoardCommands} can invoke
     * it on demand from the {@code /horde} command.
     */
    public static int spawnHordeAround(ServerLevel level, ServerPlayer target, int count) {
        RandomSource random = level.getRandom();
        // Scale the ring radius with count so 100 mobs aren't bunched on top of each
        // other. Roughly 1 mob per 2 blocks of arc on the inner ring.
        double baseRadius = Math.max(30.0, Math.min(80.0, count * 0.4));
        double radiusJitter = 10.0;

        HordeHoardConstants.LOGGER.info(
                "[horde_spawner] Spawning horde of {} mobs around {} at midnight (day {}, base radius {})",
                count, target.getName().getString(),
                Math.floorDiv(level.getOverworldClockTime(), 24000L),
                String.format("%.1f", baseRadius)
        );
        target.sendSystemMessage(
                Component.literal("§cYou hear something stirring in the dark..."), true
        );
        // Distant ominous sfx; doesn't reveal the spawn location.
        level.playSound(null, target.blockPosition(), SoundEvents.AMBIENT_CAVE.value(),
                SoundSource.HOSTILE, 1.0F, 0.5F);

        int spawned = 0;
        for (int i = 0; i < count; i++) {
            double angle = random.nextDouble() * 2.0 * Math.PI;
            double r = baseRadius + (random.nextDouble() * radiusJitter - radiusJitter / 2.0);
            double x = target.getX() + r * Math.cos(angle);
            double z = target.getZ() + r * Math.sin(angle);

            BlockPos surface = level.getHeightmapPos(
                    Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    BlockPos.containing(x, 0, z)
            );
            // Skip if the surface is far above or below the target's level (would put
            // the mob in the sky or buried in a cave).
            if (Math.abs(surface.getY() - target.blockPosition().getY()) > 32) {
                continue;
            }

            EntityType<?> type = pickMobType(random);
            Entity entity = type.spawn(level, surface, EntitySpawnReason.MOB_SUMMONED);
            if (entity == null) {
                continue;
            }
            spawned++;

            if (entity instanceof Zombie zombie && random.nextDouble() < 0.20) {
                zombie.setBaby(true);
            }
            // Force-aggro on the summoning player. Without this, mobs spawned 30-60
            // blocks away wouldn't see the player (especially when the player is in
            // creative — vanilla mobs ignore creative-mode players entirely). Without
            // a target, HordeTickProcedure's grave-robber dig branch never fires
            // because it requires Mob#getTarget() != null.
            if (entity instanceof Mob mob) {
                mob.setTarget(target);
            }
            // A small spawn puff so attentive players see arrivals at their edge of
            // perception.
            level.sendParticles(ParticleTypes.LARGE_SMOKE,
                    entity.getX(), entity.getY() + 0.5, entity.getZ(),
                    6, 0.3, 0.3, 0.3, 0.02);
        }

        HordeHoardConstants.LOGGER.info(
                "[horde_spawner] Successfully placed {}/{} mobs (some skipped due to height/load)",
                spawned, count
        );
        return spawned;
    }

    /**
     * Spawns a horde of {@code count} mobs in a tight clump (~3 block radius) about
     * 12 blocks ahead of where the target player is looking. The /horde clump entry
     * point — used when the player is actively aiming the spawn.
     */
    public static int spawnHordeClump(ServerLevel level, ServerPlayer target, int count) {
        // Project from the player's horizontal look direction so the user can aim.
        Vec3 look = target.getLookAngle();
        double horizLen = Math.sqrt(look.x * look.x + look.z * look.z);
        double dirX = horizLen > 1e-4 ? look.x / horizLen : 0.0;
        double dirZ = horizLen > 1e-4 ? look.z / horizLen : 1.0;
        double centerX = target.getX() + dirX * 12.0;
        double centerZ = target.getZ() + dirZ * 12.0;

        HordeHoardConstants.LOGGER.info(
                "[horde_spawner] Spawning clump of {} mobs ~12 blocks in front of {} (looking yaw {})",
                count, target.getName().getString(),
                String.format("%.1f", target.getYRot())
        );
        target.sendSystemMessage(
                Component.literal("§cThe ground churns ahead of you..."), true
        );
        return spawnClumpAt(level, target, count, centerX, centerZ);
    }

    /**
     * Spawns a horde of {@code count} mobs in a tight clump 18-25 blocks away from
     * the target player in a uniformly-random horizontal direction. Used by the
     * automatic midnight spawner — at midnight the player isn't aiming anywhere
     * specific, so a random ambush direction reads more naturally than projecting
     * from their look angle (which might be aimed at a chest, the sky, etc.).
     */
    public static int spawnHordeClumpAmbush(ServerLevel level, ServerPlayer target, int count) {
        RandomSource random = level.getRandom();
        double angle = random.nextDouble() * 2.0 * Math.PI;
        double distance = 18.0 + random.nextDouble() * 7.0; // 18-25 blocks
        double centerX = target.getX() + Math.cos(angle) * distance;
        double centerZ = target.getZ() + Math.sin(angle) * distance;

        HordeHoardConstants.LOGGER.info(
                "[horde_spawner] Spawning ambush clump of {} mobs around {} (day {}, distance {})",
                count, target.getName().getString(),
                Math.floorDiv(level.getOverworldClockTime(), 24000L),
                String.format("%.1f", distance)
        );
        target.sendSystemMessage(
                Component.literal("§cYou hear something stirring in the dark..."), true
        );
        return spawnClumpAt(level, target, count, centerX, centerZ);
    }

    /**
     * Shared spawn loop for the clump variants. Picks {@code count} positions
     * uniformly inside a 3-block disc around (centerX, centerZ), heightmap-snaps
     * each to the surface, and spawns a mob with force-target on {@code target}.
     */
    private static int spawnClumpAt(ServerLevel level, ServerPlayer target, int count,
                                    double centerX, double centerZ) {
        RandomSource random = level.getRandom();
        double clumpRadius = 3.0;

        level.playSound(null, target.blockPosition(), SoundEvents.AMBIENT_CAVE.value(),
                SoundSource.HOSTILE, 1.0F, 0.5F);

        int spawned = 0;
        for (int i = 0; i < count; i++) {
            // Uniform sample in disk for a natural-looking clump.
            double angle = random.nextDouble() * 2.0 * Math.PI;
            double r = clumpRadius * Math.sqrt(random.nextDouble());
            double x = centerX + r * Math.cos(angle);
            double z = centerZ + r * Math.sin(angle);

            BlockPos surface = level.getHeightmapPos(
                    Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    BlockPos.containing(x, 0, z)
            );
            if (Math.abs(surface.getY() - target.blockPosition().getY()) > 32) {
                continue;
            }

            EntityType<?> type = pickMobType(random);
            Entity entity = type.spawn(level, surface, EntitySpawnReason.MOB_SUMMONED);
            if (entity == null) {
                continue;
            }
            spawned++;

            if (entity instanceof Zombie zombie && random.nextDouble() < 0.20) {
                zombie.setBaby(true);
            }
            if (entity instanceof Mob mob) {
                mob.setTarget(target);
            }
            level.sendParticles(ParticleTypes.LARGE_SMOKE,
                    entity.getX(), entity.getY() + 0.5, entity.getZ(),
                    6, 0.3, 0.3, 0.3, 0.02);
        }

        HordeHoardConstants.LOGGER.info(
                "[horde_spawner] Successfully placed {}/{} clump mobs (some skipped due to height/load)",
                spawned, count
        );
        return spawned;
    }

    private static EntityType<?> pickMobType(RandomSource random) {
        int roll = random.nextInt(100);
        if (roll < 70) {
            return EntityType.ZOMBIE;
        } else if (roll < 80) {
            return EntityType.HUSK;
        } else if (roll < 90) {
            return EntityType.DROWNED;
        } else {
            return EntityType.ZOMBIE_VILLAGER;
        }
    }
}
