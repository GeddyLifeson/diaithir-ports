package org.diaithir.mega;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * The assay hook. Magnitude is a reading of an item's assay (DiaithirRPG c3m, data/diaithir/magnitude.json: item id to tier 0-10, depth 0-1 in the band,
 * grade A-D). Dug material goes through here: the richer the reading, the more each cubic metre yields, and the yield falls as the deposit is worked out.
 * An unassayed item reads as the middle of the band, so nothing is held back.
 */
public final class Assay {
    public record Reading(int tier, double depth, String grade) {}

    private static final Reading UNASSAYED = new Reading(0, 0.5, "D");
    private static final Map<String, Reading> READINGS = new HashMap<>();

    static {
        try (var in = Assay.class.getResourceAsStream("/data/diaithir/magnitude.json")) {
            if (in != null) {
                JsonObject root = new Gson().fromJson(new InputStreamReader(in), JsonObject.class);
                for (var e : root.entrySet()) {
                    JsonObject o = e.getValue().getAsJsonObject();
                    if (o.get("tier").isJsonNull()) continue;
                    double depth = o.get("depth").isJsonNull() ? 0.5 : o.get("depth").getAsDouble();
                    READINGS.put(e.getKey(), new Reading(o.get("tier").getAsInt(), depth, o.get("grade").isJsonNull() ? "D" : o.get("grade").getAsString()));
                }
            }
        } catch (java.io.IOException | RuntimeException ex) {
            throw new IllegalStateException("magnitude.json unreadable", ex);
        }
    }

    private Assay() {}

    public static Reading read(String itemId) { return READINGS.getOrDefault(itemId, UNASSAYED); }

    /** Items per source item for one dug m3: 0.5 to 1.5 by depth in the band, scaled 0.5 to 1 by how much of the deposit is left. */
    public static double richness(Reading r, double depositFraction) {
        return (0.5 + r.depth()) * (0.5 + 0.5 * depositFraction);
    }

    /** Count after the assay roll; at least one so a worked-out deposit still returns something. */
    public static int apply(int count, Reading r, double depositFraction) {
        return Math.max(1, (int) Math.round(count * richness(r, depositFraction)));
    }
}
