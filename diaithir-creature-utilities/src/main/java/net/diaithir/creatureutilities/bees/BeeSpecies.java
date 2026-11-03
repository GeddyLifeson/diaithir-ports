package net.diaithir.creatureutilities.bees;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.Reader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** One row of data/diaithircreatureutilities/bees/species.json. A wild species has a biome tag; a bred species has two parents. */
public record BeeSpecies(String id, String name, String biome, List<String> parents, String smeltsTo, int count) {
    public static final String DEFAULT = "ashy_mining";

    public static Map<String, BeeSpecies> parse(Reader reader) {
        JsonArray rows = JsonParser.parseReader(reader).getAsJsonObject().getAsJsonArray("species");
        Map<String, BeeSpecies> out = new LinkedHashMap<>();
        for (JsonElement e : rows) {
            JsonObject o = e.getAsJsonObject();
            String id = o.get("id").getAsString();
            if (!id.matches("[a-z0-9_]+") || out.containsKey(id)) {
                throw new IllegalArgumentException("bad or duplicate species id " + id);
            }
            List<String> parents = new ArrayList<>();
            if (o.has("parents") && o.get("parents").isJsonArray()) {
                o.getAsJsonArray("parents").forEach(p -> parents.add(p.getAsString()));
            }
            String biome = o.has("biome") && !o.get("biome").isJsonNull() ? o.get("biome").getAsString() : null;
            if (biome == null && parents.size() != 2) {
                throw new IllegalArgumentException("species " + id + " needs a biome tag or two parents");
            }
            for (String p : parents) {
                if (!out.containsKey(p)) {
                    throw new IllegalArgumentException("species " + id + " has parent " + p + " that is not listed before it");
                }
            }
            out.put(id, new BeeSpecies(id, o.get("name").getAsString(), biome, List.copyOf(parents), o.get("smelts_to").getAsString(), o.get("count").getAsInt()));
        }
        return out;
    }

    public boolean wild() {
        return biome != null;
    }
}
