package net.diaithir.compat;

import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.Mob;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import org.slf4j.Logger;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Refuses natural spawns of entities outside their geographic range, on the Tellus Earth dimension only.
 *
 * <p>Natural means the mob was finalized with {@link EntitySpawnReason#NATURAL} or {@link EntitySpawnReason#CHUNK_GENERATION}
 * (vanilla spawners, world generation and Untamed Wilds' own spawner all do this); summons, spawn eggs, spawners, breeding
 * and entities loaded from disk are never touched. The check runs when the new mob joins the level, so a variant set during
 * spawning (Untamed Wilds sets its species before adding the mob) is already known.
 */
public final class EarthRangeGate {
    private static final Logger LOG = LogUtils.getLogger();
    static final EarthRanges RANGES = new EarthRanges();
    private static final Map<Class<?>, Method[]> SPECIES_METHODS = new ConcurrentHashMap<>();
    private static final Method[] NONE = new Method[0];
    private static volatile Object cachedGenerator;
    private static volatile TellusBridge cachedBridge;

    private EarthRangeGate() {
    }

    static void onAddReloadListeners(AddServerReloadListenersEvent event) {
        event.addListener(Identifier.fromNamespaceAndPath("diaithircompat", "earth_ranges"), new Loader());
    }

    private static final class Loader extends SimpleJsonResourceReloadListener<JsonElement> {
        Loader() {
            super(ExtraCodecs.JSON, FileToIdConverter.json("earth_ranges"));
        }

        @Override
        protected void apply(Map<Identifier, JsonElement> files, ResourceManager manager, ProfilerFiller profiler) {
            List<EarthRanges.Entry> all = new ArrayList<>();
            for (Map.Entry<Identifier, JsonElement> file : files.entrySet()) {
                all.addAll(EarthRanges.parse(file.getValue(), msg -> LOG.warn("earth_ranges {}: {}", file.getKey(), msg)));
            }
            RANGES.replace(all);
            LOG.info("Loaded {} Earth range entries for {} files", RANGES.entryCount(), files.size());
        }
    }

    static void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.loadedFromDisk() || RANGES.isEmpty() || !(event.getLevel() instanceof ServerLevel level) || !(event.getEntity() instanceof Mob mob)) {
            return;
        }
        EntitySpawnReason reason = mob.getSpawnType();
        if (reason != EntitySpawnReason.NATURAL && reason != EntitySpawnReason.CHUNK_GENERATION) {
            return;
        }
        String id = BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType()).toString();
        if (!RANGES.hasEntity(id)) {
            return;
        }
        Object generator = level.getChunkSource().getGenerator();
        if (!TellusBridge.isEarthGenerator(generator)) {
            return;
        }
        TellusBridge bridge = bridge(generator);
        double[] ll = bridge == null ? null : bridge.latLon(generator, mob.getX(), mob.getZ());
        if (ll == null) {
            return;
        }
        int variant = -1;
        String species = null;
        Method[] m = SPECIES_METHODS.computeIfAbsent(mob.getClass(), EarthRangeGate::speciesMethods);
        if (m.length == 2) {
            try {
                variant = (int) m[0].invoke(mob);
                species = String.valueOf(m[1].invoke(mob, variant)).toLowerCase(Locale.ROOT);
            } catch (ReflectiveOperationException | RuntimeException ignored) {
                variant = -1;
                species = null;
            }
        }
        if (!RANGES.allows(id, variant, species, ll[0], ll[1])) {
            event.setCanceled(true);
            LOG.debug("Refused natural spawn of {} ({}) at lat {} lon {}", id, species, ll[0], ll[1]);
        }
    }

    private static TellusBridge bridge(Object generator) {
        if (cachedGenerator != generator) {
            cachedBridge = TellusBridge.forGenerator(generator);
            cachedGenerator = generator;
        }
        return cachedBridge;
    }

    // Untamed Wilds' ComplexMob exposes getVariant() and ISpecies#getRawSpeciesName(int); other mobs have neither.
    private static Method[] speciesMethods(Class<?> c) {
        try {
            return new Method[] {c.getMethod("getVariant"), c.getMethod("getRawSpeciesName", int.class)};
        } catch (ReflectiveOperationException e) {
            return NONE;
        }
    }
}
