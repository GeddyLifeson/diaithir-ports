package net.diaithir.fauna.bees;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/** One bee species: where wild ones come from, which two parents cross into it, and what its comb is worth. Table lives in data/diaithirfauna/bees/species.json. */
public record BeeSpecies(String id, String name, int wild, String parentA, String parentB, String outItem, int outCount, String method, int color, int age) {
    public static final List<BeeSpecies> ALL = load();
    public static final String DEFAULT = "meadow";

    private static List<BeeSpecies> load() {
        List<BeeSpecies> out = new ArrayList<>();
        try (var in = new InputStreamReader(BeeSpecies.class.getResourceAsStream("/data/diaithirfauna/bees/species.json"))) {
            JsonArray arr = new Gson().fromJson(in, JsonArray.class);
            for (JsonElement e : arr) {
                JsonObject o = e.getAsJsonObject(), r = o.getAsJsonObject("out");
                JsonArray c = o.get("cross").isJsonNull() ? null : o.getAsJsonArray("cross");
                out.add(new BeeSpecies(o.get("id").getAsString(), o.get("name").getAsString(), o.get("wild").getAsInt(),
                        c == null ? "" : c.get(0).getAsString(), c == null ? "" : c.get(1).getAsString(),
                        r.get("item").getAsString(), r.get("count").getAsInt(), r.get("method").getAsString(),
                        Integer.parseInt(o.get("color").getAsString(), 16), o.get("age").getAsInt()));
            }
        } catch (Exception ex) {
            throw new IllegalStateException("bee species table", ex);
        }
        return List.copyOf(out);
    }

    public static BeeSpecies byId(String id) {
        for (BeeSpecies s : ALL) if (s.id().equals(id)) return s;
        return null;
    }

    public boolean isCross() { return !parentA().isEmpty(); }

    /** The species two parents cross into, or null when they have no cross. Order does not matter. */
    public static BeeSpecies crossOf(String a, String b) {
        for (BeeSpecies s : ALL) {
            if (s.isCross() && ((s.parentA().equals(a) && s.parentB().equals(b)) || (s.parentA().equals(b) && s.parentB().equals(a)))) return s;
        }
        return null;
    }
}
