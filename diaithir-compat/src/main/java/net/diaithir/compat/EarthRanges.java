package net.diaithir.compat;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Geographic ranges for entities, read from {@code data/<namespace>/earth_ranges/*.json}.
 *
 * <p>A file is either one range object or {@code {"ranges": [ ... ]}}. A range object is
 * <pre>
 * { "entity": "untamedwilds:big_cat", "variant": 9, "species": "american_lion",
 *   "boxes": [ { "lat": [min, max], "lon": [min, max] } ] }
 * </pre>
 * {@code variant} (an integer) and {@code species} (a lower-case species name) are optional and narrow the range to one
 * species variant of the entity. A box whose {@code lon} minimum is above its maximum wraps across the antimeridian.
 *
 * <p>Matching: if any range names the entity's own variant or species, only those ranges apply; otherwise ranges without a
 * variant apply; with no range at all the entity is unaffected.
 */
public final class EarthRanges {
    public record Box(double latMin, double latMax, double lonMin, double lonMax) {
        public boolean contains(double lat, double lon) {
            if (lat < latMin || lat > latMax) {
                return false;
            }
            return lonMin <= lonMax ? lon >= lonMin && lon <= lonMax : lon >= lonMin || lon <= lonMax;
        }
    }

    public record Entry(String entity, Integer variant, String species, List<Box> boxes) {
        boolean isSpecific() {
            return variant != null || species != null;
        }

        boolean matches(int entityVariant, String entitySpecies) {
            return (variant != null && variant == entityVariant) || (species != null && species.equals(entitySpecies));
        }

        boolean contains(double lat, double lon) {
            for (Box b : boxes) {
                if (b.contains(lat, lon)) {
                    return true;
                }
            }
            return false;
        }
    }

    private volatile Map<String, List<Entry>> byEntity = Map.of();

    public void replace(List<Entry> entries) {
        Map<String, List<Entry>> map = new HashMap<>();
        for (Entry e : entries) {
            map.computeIfAbsent(e.entity(), k -> new ArrayList<>()).add(e);
        }
        this.byEntity = map;
    }

    public boolean isEmpty() {
        return this.byEntity.isEmpty();
    }

    public boolean hasEntity(String entityId) {
        return this.byEntity.containsKey(entityId);
    }

    public int entryCount() {
        return this.byEntity.values().stream().mapToInt(List::size).sum();
    }

    /** True when an entity (with this variant and species name, -1 and null when unknown) may exist at the given latitude and longitude. */
    public boolean allows(String entityId, int variant, String species, double lat, double lon) {
        List<Entry> list = this.byEntity.get(entityId);
        if (list == null) {
            return true;
        }
        boolean anySpecific = false;
        for (Entry e : list) {
            if (e.isSpecific() && e.matches(variant, species)) {
                anySpecific = true;
                if (e.contains(lat, lon)) {
                    return true;
                }
            }
        }
        if (anySpecific) {
            return false;
        }
        boolean anyGeneral = false;
        for (Entry e : list) {
            if (!e.isSpecific()) {
                anyGeneral = true;
                if (e.contains(lat, lon)) {
                    return true;
                }
            }
        }
        return !anyGeneral;
    }

    /** Parses one file; problems are reported to {@code onError} and the offending range is skipped. */
    public static List<Entry> parse(JsonElement json, Consumer<String> onError) {
        List<Entry> out = new ArrayList<>();
        if (json == null || !json.isJsonObject()) {
            onError.accept("not a JSON object");
            return out;
        }
        JsonObject root = json.getAsJsonObject();
        if (root.has("ranges") && root.get("ranges").isJsonArray()) {
            for (JsonElement e : root.getAsJsonArray("ranges")) {
                parseEntry(e, out, onError);
            }
        } else {
            parseEntry(root, out, onError);
        }
        return out;
    }

    private static void parseEntry(JsonElement el, List<Entry> out, Consumer<String> onError) {
        try {
            JsonObject o = el.getAsJsonObject();
            String entity = o.get("entity").getAsString();
            Integer variant = o.has("variant") ? o.get("variant").getAsInt() : null;
            String species = o.has("species") ? o.get("species").getAsString().toLowerCase(Locale.ROOT) : null;
            List<Box> boxes = new ArrayList<>();
            JsonArray arr = o.getAsJsonArray("boxes");
            for (JsonElement b : arr) {
                JsonObject bo = b.getAsJsonObject();
                JsonArray lat = bo.getAsJsonArray("lat");
                JsonArray lon = bo.getAsJsonArray("lon");
                boxes.add(new Box(lat.get(0).getAsDouble(), lat.get(1).getAsDouble(), lon.get(0).getAsDouble(), lon.get(1).getAsDouble()));
            }
            if (boxes.isEmpty()) {
                onError.accept("range for " + entity + " has no boxes");
                return;
            }
            out.add(new Entry(entity, variant, species, List.copyOf(boxes)));
        } catch (RuntimeException e) {
            onError.accept("bad range entry: " + e);
        }
    }
}
