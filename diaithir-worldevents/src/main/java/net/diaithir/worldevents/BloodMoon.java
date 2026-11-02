package net.diaithir.worldevents;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;

import java.util.List;

/** Until dawn, monsters swarm every player at night, with a stronger wave every five minutes. */
final class BloodMoon implements WorldEvent {
    static final BloodMoon INSTANCE = new BloodMoon();
    static final String TAG = "diaithir_blood_moon";
    private static final int WAVE_GAP = 6000;
    private static final List<EntityType<? extends Mob>> MOBS = List.of(
            EntityType.ZOMBIE, EntityType.SKELETON, EntityType.SPIDER, EntityType.WITCH, EntityType.CREEPER);
    private static boolean active;
    private static long nextWave;

    static boolean active() {
        return active;
    }

    static void reset() {
        active = false;
    }

    static void tick(ServerLevel level) {
        if (!active) return;
        if (!level.isDarkOutside()) {
            active = false;
            WorldEvents.announce(level.getServer(), "The Blood Moon fades with the sunrise.", ChatFormatting.GRAY);
            return;
        }
        if (level.getServer().getTickCount() >= nextWave) {
            nextWave += WAVE_GAP;
            WorldEvents.announce(level.getServer(), "The Blood Moon reaches its peak. The horde grows stronger.", ChatFormatting.DARK_RED);
            wave(level);
        }
    }

    private static int wave(ServerLevel level) {
        int spawned = 0;
        for (ServerPlayer player : level.players()) {
            for (int i = 0; i < 6; i++) {
                BlockPos p = level.getHeightmapPos(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                        player.blockPosition().offset(WorldEvents.RANDOM.nextInt(-24, 25), 0, WorldEvents.RANDOM.nextInt(-24, 25)));
                if (level.getMaxLocalRawBrightness(p) > 7) continue;
                Mob mob = MOBS.get(WorldEvents.RANDOM.nextInt(MOBS.size())).create(level, EntitySpawnReason.EVENT);
                if (mob == null) continue;
                mob.snapTo(p.getX() + 0.5, p.getY(), p.getZ() + 0.5, WorldEvents.RANDOM.nextFloat() * 360F, 0F);
                mob.finalizeSpawn(level, level.getCurrentDifficultyAt(p), EntitySpawnReason.EVENT, null);
                mob.addTag(TAG);
                level.addFreshEntity(mob);
                spawned++;
            }
        }
        return spawned;
    }

    @Override public String id() { return "blood_moon"; }
    @Override public String title() { return "Blood Moon"; }
    @Override public int weight() { return 15; }
    @Override public boolean possible(ServerLevel level) { return !active && level.isDarkOutside(); }

    @Override
    public String trigger(ServerLevel level, List<ServerPlayer> players) {
        if (players.isEmpty()) return "No player in the overworld.";
        active = true;
        nextWave = level.getServer().getTickCount() + WAVE_GAP;
        WorldEvents.announce(level.getServer(), "The moon rises blood red. Something is very wrong tonight...", ChatFormatting.DARK_RED);
        return "Blood Moon wave 1: " + wave(level) + " monsters";
    }
}
