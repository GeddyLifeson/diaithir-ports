package net.diaithir.woodvariants;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.Reader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** One row of woods.json: a wood from some mod and the textures its variants wear. */
public record WoodDef(String id, String mod, String name, String gen, String planks, String log,
                      String planksTexture, String logTexture, String logTopTexture) {

    public static List<WoodDef> parse(Reader reader) {
        JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
        JsonArray rows = root.getAsJsonArray("woods");
        List<WoodDef> out = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (JsonElement e : rows) {
            JsonObject o = e.getAsJsonObject();
            WoodDef w = new WoodDef(str(o, "id"), str(o, "mod"), str(o, "name"), o.has("gen") ? o.get("gen").getAsString() : "", str(o, "planks"), str(o, "log"),
                str(o, "planks_texture"), str(o, "log_texture"), str(o, "log_top_texture"));
            if (!w.id.matches("[a-z0-9_]+")) {
                throw new IllegalArgumentException("bad wood id " + w.id);
            }
            if (!seen.add(w.id)) {
                throw new IllegalArgumentException("duplicate wood id " + w.id);
            }
            out.add(w);
        }
        return out;
    }

    private static String str(JsonObject o, String key) {
        if (!o.has(key) || o.get(key).getAsString().isBlank()) {
            throw new IllegalArgumentException("wood row missing " + key + ": " + o);
        }
        return o.get(key).getAsString();
    }
}
