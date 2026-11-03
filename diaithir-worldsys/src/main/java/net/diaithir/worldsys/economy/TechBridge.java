package net.diaithir.worldsys.economy;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Each culture's own tech Age and known techs, from the tech tree. The tree runs in KubeJS (claude/tech-tree) and a small publisher script writes
 * kubejs/data/tech_published.json ({"eras": {culture: age}, "known": {culture: [tech ids]}}). This reads it when it is there and falls back to the
 * world Age and "every tech known" when it is not, so the economy works whether or not the tree is installed. Cultures the file does not list use the same fallbacks.
 */
public final class TechBridge {
    private TechBridge() {}

    private static Path file;
    private static long mtime = Long.MIN_VALUE;
    private static long checked = Long.MIN_VALUE;
    private static final Map<String, String> ERAS = new HashMap<>();
    private static final Map<String, Set<String>> KNOWN = new HashMap<>();

    /** Where the publisher writes (the server's game directory). */
    public static void init(Path gameDir) {
        file = gameDir.resolve("kubejs").resolve("data").resolve("tech_published.json");
        mtime = Long.MIN_VALUE;
        EconHooks.ageOf = TechBridge::ageOf;
        EconHooks.techKnown = TechBridge::known;
    }

    static synchronized void refresh() {
        long now = System.currentTimeMillis();
        if (file == null || now - checked < 30_000 && mtime != Long.MIN_VALUE) return;
        checked = now;
        try {
            if (!Files.exists(file)) { ERAS.clear(); KNOWN.clear(); return; }
            long m = Files.getLastModifiedTime(file).toMillis();
            if (m == mtime) return;
            JsonObject o = JsonParser.parseString(Files.readString(file, StandardCharsets.UTF_8)).getAsJsonObject();
            ERAS.clear(); KNOWN.clear();
            if (o.has("eras")) for (var e : o.getAsJsonObject("eras").entrySet()) ERAS.put(e.getKey(), e.getValue().getAsString());
            if (o.has("known")) for (var e : o.getAsJsonObject("known").entrySet()) {
                Set<String> s = new HashSet<>();
                for (JsonElement t : e.getValue().getAsJsonArray()) s.add(t.getAsString());
                KNOWN.put(e.getKey(), s);
            }
            mtime = m;
        } catch (IOException | RuntimeException ex) {
            System.out.println("[economy] tech file unreadable: " + ex);
        }
    }

    /** The culture's own Age, or null when the tree does not say (the caller falls back to the world Age). */
    public static String ageOf(String culture) {
        refresh();
        String a = ERAS.get(culture);
        return a != null && EconPolity.AGES.contains(a) ? a : null;
    }

    /** Does the culture know the tech? True when the tree does not list the culture, so a pack without the tree gates nothing. */
    public static boolean known(String culture, String tech) {
        refresh();
        Set<String> s = KNOWN.get(culture);
        return s == null || s.contains(tech);
    }

    /** The Age a good with an opening Age and a tier is gated at: opens_age + tier - 1, capped at the last Age. */
    public static String gateAge(String opensAge, int tier) {
        int i = EconPolity.AGES.indexOf(opensAge);
        if (i < 0) return "I";
        return EconPolity.AGES.get(Math.min(EconPolity.AGES.size() - 1, i + Math.max(1, tier) - 1));
    }
}
