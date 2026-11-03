// Ported from Extreme Sound Muffler (LGPL-3.0-only) https://github.com/LeoBeliik/ExtremeSoundMuffler
// Modifications: Merged Constants/ISoundLists/DataManager into one class; JSON persistence of the per-sound volume map only (anchors dropped).
package net.diaithir.audio.soundmuffler.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mojang.logging.LogUtils;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.neoforged.fml.loading.FMLPaths;
import org.slf4j.Logger;

public final class MufflerData {
    private static final Logger LOG = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type MAP_TYPE = new TypeToken<Map<String, Double>>() {}.getType();

    /** sound id (or mod namespace) -> volume multiplier. */
    public static final Map<String, Double> MUFFLED = new ConcurrentHashMap<>();
    public static final List<String> RECENT = new CopyOnWriteArrayList<>();
    public static final Map<String, List<String>> BLOCK_SOUNDS = new TreeMap<>();
    public static final TreeSet<String> ALL_SOUNDS = new TreeSet<>();
    public static final TreeSet<String> MODS = new TreeSet<>();
    private static final Map<String, Boolean> FORBIDDEN_CACHE = new ConcurrentHashMap<>();

    /** Master switch for muffling. */
    public static volatile boolean muffling = true;
    /** True while the GUI preview button plays a sound (never muffled). */
    public static volatile boolean fromGui;
    private static boolean loaded;
    private static boolean cachesBuilt;

    private MufflerData() {}

    private static Path dir() {
        return FMLPaths.GAMEDIR.get().resolve("ESM");
    }

    public static void ensureLoaded() {
        if (!loaded) reload();
        buildCaches();
    }

    public static void reload() {
        MUFFLED.clear();
        Path f = dir().resolve("soundsMuffled.dat");
        if (Files.isRegularFile(f)) {
            try {
                Map<String, Double> m = GSON.fromJson(Files.readString(f, StandardCharsets.UTF_8), MAP_TYPE);
                if (m != null) m.forEach((k, v) -> {
                    if (k != null && v != null) MUFFLED.put(k, v);
                });
            } catch (Exception e) {
                LOG.error("ESM: error loading the muffled sounds list", e);
            }
        }
        loaded = true;
    }

    public static void save() {
        try {
            Path d = dir();
            Files.createDirectories(d);
            Files.writeString(d.resolve("soundsMuffled.dat"), GSON.toJson(MUFFLED), StandardCharsets.UTF_8);
        } catch (Exception e) {
            LOG.error("ESM: error saving the muffled sounds list", e);
        }
    }

    private static void buildCaches() {
        if (cachesBuilt) return;
        cachesBuilt = true;
        BuiltInRegistries.SOUND_EVENT.forEach(ev -> {
            String id = ev.location().toString();
            ALL_SOUNDS.add(id);
            MODS.add(ev.location().getNamespace());
        });
        for (Block block : BuiltInRegistries.BLOCK) {
            if (block.defaultBlockState().isAir()) continue;
            SoundType t = block.defaultBlockState().getSoundType();
            BLOCK_SOUNDS.put(block.getName().getString(), List.of(
                    t.getBreakSound().location().toString(),
                    t.getFallSound().location().toString(),
                    t.getHitSound().location().toString(),
                    t.getPlaceSound().location().toString(),
                    t.getStepSound().location().toString()));
        }
    }

    public static void clearForbiddenCache() {
        FORBIDDEN_CACHE.clear();
    }

    public static boolean isForbidden(String soundId) {
        return FORBIDDEN_CACHE.computeIfAbsent(soundId, id -> false);
    }

    public static void recordRecent(String soundId) {
        RECENT.remove(soundId);
        RECENT.add(soundId);
        while (RECENT.size() > 300) RECENT.remove(0);
    }
}
