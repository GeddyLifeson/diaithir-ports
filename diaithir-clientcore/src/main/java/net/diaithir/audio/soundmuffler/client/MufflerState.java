// Ported from Extreme Sound Muffler (LGPL-3.0-only) https://github.com/LeoBeliik/ExtremeSoundMuffler
// Modifications: Replaces ISoundLists/DataManager; state persisted in the NeoForge client config.
package net.diaithir.audio.soundmuffler.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.registries.BuiltInRegistries;

public final class MufflerState {
    private static final Map<String, Double> MUFFLED = new ConcurrentHashMap<>();
    private static final List<String> RECENT = new ArrayList<>();
    private static final Map<String, Boolean> FORBIDDEN_CACHE = new ConcurrentHashMap<>();
    private static final Set<String> ALL_SOUNDS = new LinkedHashSet<>();
    private static boolean loaded;

    private MufflerState() {}

    public static void load() {
        MUFFLED.clear();
        for (String s : MufflerConfig.MUFFLED.get()) {
            int eq = s.lastIndexOf('=');
            if (eq <= 0) continue;
            try {
                MUFFLED.put(s.substring(0, eq), Math.max(0.0D, Math.min(1.0D, Double.parseDouble(s.substring(eq + 1)))));
            } catch (NumberFormatException ignored) {
            }
        }
        FORBIDDEN_CACHE.clear();
        loaded = true;
    }

    public static void ensureLoaded() {
        if (!loaded) load();
    }

    public static void clearForbiddenCache() {
        FORBIDDEN_CACHE.clear();
    }

    public static void save() {
        List<String> out = new ArrayList<>();
        for (Map.Entry<String, Double> e : MUFFLED.entrySet()) {
            out.add(e.getKey() + "=" + String.format(Locale.ROOT, "%.3f", e.getValue()));
        }
        MufflerConfig.MUFFLED.set(out);
        MufflerConfig.SPEC.save();
    }

    public static boolean isEnabled() {
        return MufflerConfig.MUFFLING_ENABLED.get();
    }

    public static void setEnabled(boolean v) {
        MufflerConfig.MUFFLING_ENABLED.set(v);
        MufflerConfig.SPEC.save();
    }

    public static Map<String, Double> muffled() {
        return MUFFLED;
    }

    public static Double volumeFor(String soundId, String namespace) {
        Double v = MUFFLED.get(soundId);
        if (v != null) return v;
        return MUFFLED.get(namespace);
    }

    public static boolean isForbidden(String id) {
        List<? extends String> list = MufflerConfig.FORBIDDEN_SOUNDS.get();
        if (list.isEmpty()) return false;
        return FORBIDDEN_CACHE.computeIfAbsent(id, k -> {
            for (String fs : MufflerConfig.FORBIDDEN_SOUNDS.get()) {
                if (!fs.isEmpty() && k.contains(fs)) return true;
            }
            return false;
        });
    }

    public static synchronized void recordRecent(String id) {
        RECENT.remove(id);
        RECENT.add(id);
        if (RECENT.size() > 200) RECENT.remove(0);
    }

    public static synchronized List<String> recent() {
        List<String> copy = new ArrayList<>(RECENT);
        java.util.Collections.reverse(copy);
        return copy;
    }

    public static synchronized void clearRecent() {
        RECENT.clear();
    }

    public static Set<String> allSounds() {
        if (ALL_SOUNDS.isEmpty()) {
            BuiltInRegistries.SOUND_EVENT.forEach(e -> ALL_SOUNDS.add(BuiltInRegistries.SOUND_EVENT.getKey(e).toString()));
        }
        return ALL_SOUNDS;
    }

    public static Map<String, Double> snapshot() {
        return new HashMap<>(MUFFLED);
    }
}
