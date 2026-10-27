package diaithir.myth;

import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.ServerLevelAccessor;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Keeps natural spawns inside each creature's court region and Age window.
 *
 * <p>Region: only on the Tellus Earth chunk generator (found by class name, so Tellus is not a dependency); the block
 * position becomes latitude/longitude with {@link EarthGeo} at the generator's own world scale (5 if unreadable). In any
 * other world (flat, vanilla, the island dimension) the region test is skipped.
 *
 * <p>Age: read from the diaithir_ages mod by reflection (AgesSavedData.get(server).world.age), a soft dependency. When that
 * mod is absent or unreadable the world counts as Age III. Cached for ten seconds.
 *
 * <p>Only NATURAL and CHUNK_GENERATION spawns are gated; commands, eggs, spawners, structures and saved mobs are not.
 */
public final class SpawnGate {
    private static final Logger LOG = LogUtils.getLogger();
    public static final int DEFAULT_AGE = 3;
    static final SpawnRegions REGIONS = new SpawnRegions();

    private static volatile Object cachedGenerator;
    private static volatile double cachedScale;
    private static volatile int cachedAge = DEFAULT_AGE;
    private static volatile int cachedAgeTick = Integer.MIN_VALUE;
    private static volatile boolean agesBroken;
    private static Method agesGet;
    private static Field agesWorld, agesAge;

    private SpawnGate() {
    }

    static void onAddReloadListeners(AddServerReloadListenersEvent event) {
        event.addListener(Identifier.fromNamespaceAndPath(DiaithirMyth.MODID, "spawn_regions"), new Loader());
    }

    private static final class Loader extends SimpleJsonResourceReloadListener<JsonElement> {
        Loader() {
            super(ExtraCodecs.JSON, FileToIdConverter.json("spawn_regions"));
        }

        @Override
        protected void apply(Map<Identifier, JsonElement> files, ResourceManager manager, ProfilerFiller profiler) {
            List<SpawnRegions.Region> all = new ArrayList<>();
            for (Map.Entry<Identifier, JsonElement> f : files.entrySet()) {
                SpawnRegions.Region r = SpawnRegions.parse(f.getKey().toString(), f.getValue(), msg -> LOG.warn("spawn_regions {}: {}", f.getKey(), msg));
                if (r != null) {
                    all.add(r);
                }
            }
            REGIONS.replace(all);
            LOG.info("Loaded {} spawn regions", all.size());
        }
    }

    static boolean gated(EntitySpawnReason reason) {
        return reason == EntitySpawnReason.NATURAL || reason == EntitySpawnReason.CHUNK_GENERATION;
    }

    static void onFinalizeSpawn(FinalizeSpawnEvent event) {
        if (!gated(event.getSpawnType()) || !(event.getEntity() instanceof MythBeast b)) {
            return;
        }
        if (!allows(b.def.id, event.getLevel(), event.getX(), event.getZ())) {
            event.setSpawnCancelled(true);
        }
    }

    /** True when the creature may spawn naturally at this block column, at the world's current Age. */
    public static boolean allows(String creatureId, ServerLevelAccessor level, double blockX, double blockZ) {
        if (REGIONS.get(creatureId) == null) {
            return true;
        }
        ServerLevel sl = level.getLevel();
        Object gen = sl.getChunkSource().getGenerator();
        if (!isEarthGenerator(gen)) {
            return true;
        }
        double scale = worldScale(gen);
        if (!REGIONS.allowsPlace(creatureId, EarthGeo.latitude(blockZ, scale), EarthGeo.longitude(blockX, blockZ, scale))) {
            return false;
        }
        return REGIONS.allowsAge(creatureId, currentAge(sl.getServer()));
    }

    private static boolean isEarthGenerator(Object gen) {
        for (Class<?> c = gen.getClass(); c != null; c = c.getSuperclass()) {
            if (c.getName().equals("com.yucareux.tellus.worldgen.EarthChunkGenerator")) {
                return true;
            }
        }
        return false;
    }

    private static double worldScale(Object gen) {
        if (cachedGenerator != gen) {
            double scale = EarthGeo.DEFAULT_WORLD_SCALE;
            try {
                Object settings = gen.getClass().getMethod("settings").invoke(gen);
                scale = (double) settings.getClass().getMethod("worldScale").invoke(settings);
            } catch (ReflectiveOperationException | RuntimeException e) {
                LOG.warn("cannot read the Tellus world scale, assuming {}", scale);
            }
            cachedScale = scale;
            cachedGenerator = gen;
        }
        return cachedScale;
    }

    /** The world's Age as 0 (the Telling) to 7, or {@link #DEFAULT_AGE} when diaithir_ages is absent or unreadable. */
    public static int currentAge(MinecraftServer server) {
        int tick = server.getTickCount();
        if (agesBroken || (cachedAgeTick != Integer.MIN_VALUE && tick - cachedAgeTick >= 0 && tick - cachedAgeTick < 200)) {
            return cachedAge;
        }
        cachedAgeTick = tick;
        try {
            if (agesGet == null) {
                if (!ModList.get().isLoaded("diaithir_ages")) {
                    agesBroken = true;
                    return cachedAge = DEFAULT_AGE;
                }
                Class<?> saved;
                try {
                    saved = Class.forName("net.diaithir.ages.mc.AgesSavedData", true, Thread.currentThread().getContextClassLoader());
                } catch (ClassNotFoundException e) {
                    saved = Class.forName("net.diaithir.ages.mc.AgesSavedData", true, SpawnGate.class.getClassLoader());
                }
                agesGet = saved.getMethod("get", MinecraftServer.class);
                agesWorld = saved.getField("world");
                agesAge = agesWorld.getType().getField("age");
            }
            Object data = agesGet.invoke(null, server);
            int n = SpawnRegions.ageNumber((String) agesAge.get(agesWorld.get(data)));
            cachedAge = n < 0 ? DEFAULT_AGE : n;
        } catch (ReflectiveOperationException | RuntimeException | LinkageError e) {
            LOG.warn("cannot read the Age from diaithir_ages, treating the world as Age {}: {}", DEFAULT_AGE, e.toString());
            agesBroken = true;
            cachedAge = DEFAULT_AGE;
        }
        return cachedAge;
    }
}
