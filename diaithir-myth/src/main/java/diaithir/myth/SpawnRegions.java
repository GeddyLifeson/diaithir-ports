package diaithir.myth;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Where and when each creature may spawn naturally, from data/diaithir_myth/spawn_regions/&lt;creature&gt;.json:
 * <pre>
 * { "creature": "diaithir_myth:tarbmoir", "min_age": 2, "max_age": 7 (optional),
 *   "boxes": [ { "lat": [min, max], "lon": [min, max] } ] }
 * </pre>
 * A box whose lon minimum is above its maximum wraps the antimeridian. A creature without a file is unrestricted; one
 * with an empty box list never spawns naturally on Earth. No Minecraft classes here, so it is unit tested.
 */
public final class SpawnRegions {
    public record Box(double latMin, double latMax, double lonMin, double lonMax) {
        public boolean contains(double lat, double lon) {
            if (lat < latMin || lat > latMax) {
                return false;
            }
            return lonMin <= lonMax ? lon >= lonMin && lon <= lonMax : lon >= lonMin || lon <= lonMax;
        }
    }

    public record Region(String creature, int minAge, int maxAge, List<Box> boxes) {
        public boolean containsPoint(double lat, double lon) {
            for (Box b : boxes) {
                if (b.contains(lat, lon)) {
                    return true;
                }
            }
            return false;
        }

        public boolean ageOpen(int age) {
            return age >= minAge && age <= maxAge;
        }
    }

    private volatile Map<String, Region> byCreature = Map.of();

    public void replace(List<Region> regions) {
        Map<String, Region> m = new HashMap<>();
        for (Region r : regions) {
            m.put(r.creature(), r);
        }
        this.byCreature = m;
    }

    public boolean isEmpty() {
        return byCreature.isEmpty();
    }

    public Region get(String creatureId) {
        return byCreature.get(creatureId);
    }

    /** True when the creature has no region data, or the point lies in one of its boxes. */
    public boolean allowsPlace(String creatureId, double lat, double lon) {
        Region r = byCreature.get(creatureId);
        return r == null || r.containsPoint(lat, lon);
    }

    /** True when the creature has no region data, or the Age (0 = the Telling, 1 to 7 = Ages I to VII) is in its window. */
    public boolean allowsAge(String creatureId, int age) {
        Region r = byCreature.get(creatureId);
        return r == null || r.ageOpen(age);
    }

    /** Parses one file; a problem goes to onError and yields null. */
    public static Region parse(String fallbackId, JsonElement json, Consumer<String> onError) {
        try {
            JsonObject o = json.getAsJsonObject();
            String id = o.has("creature") ? o.get("creature").getAsString() : fallbackId;
            int min = o.has("min_age") ? o.get("min_age").getAsInt() : 0;
            int max = o.has("max_age") ? o.get("max_age").getAsInt() : Integer.MAX_VALUE;
            List<Box> boxes = new ArrayList<>();
            JsonArray arr = o.getAsJsonArray("boxes");
            for (JsonElement b : arr) {
                JsonObject bo = b.getAsJsonObject();
                JsonArray lat = bo.getAsJsonArray("lat");
                JsonArray lon = bo.getAsJsonArray("lon");
                boxes.add(new Box(lat.get(0).getAsDouble(), lat.get(1).getAsDouble(), lon.get(0).getAsDouble(), lon.get(1).getAsDouble()));
            }
            return new Region(id, min, max, List.copyOf(boxes));
        } catch (RuntimeException e) {
            onError.accept("bad spawn region: " + e);
            return null;
        }
    }

    /** Age id from diaithir_ages to a number: TELLING 0, I to VII 1 to 7; anything else -1. */
    public static int ageNumber(String id) {
        if (id == null) {
            return -1;
        }
        return switch (id.trim().toUpperCase(java.util.Locale.ROOT)) {
            case "TELLING" -> 0;
            case "I" -> 1;
            case "II" -> 2;
            case "III" -> 3;
            case "IV" -> 4;
            case "V" -> 5;
            case "VI" -> 6;
            case "VII" -> 7;
            default -> -1;
        };
    }
}
