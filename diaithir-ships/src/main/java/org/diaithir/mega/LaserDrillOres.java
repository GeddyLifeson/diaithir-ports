package org.diaithir.mega;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import org.diaithir.mega.LaserDrill.Rarity;
import org.diaithir.mega.LaserDrill.Row;

/** Ore rows for the laser drill from data/megamachines/laser_drill/ores.json (values from Industrial Foregoing's default recipes, MIT). */
public final class LaserDrillOres {
    private static final List<Row> ROWS = new ArrayList<>();

    static {
        try (var in = LaserDrillOres.class.getResourceAsStream("/data/megamachines/laser_drill/ores.json")) {
            JsonArray a = new Gson().fromJson(new InputStreamReader(in), JsonObject.class).getAsJsonArray("rows");
            for (var e : a) {
                JsonObject o = e.getAsJsonObject();
                List<Rarity> rs = new ArrayList<>();
                for (var r : o.getAsJsonArray("rarity")) {
                    JsonObject ro = r.getAsJsonObject();
                    rs.add(new Rarity(strs(ro, "biome_whitelist"), strs(ro, "biome_blacklist"), strs(ro, "dim_whitelist"), strs(ro, "dim_blacklist"),
                            ro.get("depth_min").getAsInt(), ro.get("depth_max").getAsInt(), ro.get("weight").getAsInt()));
                }
                ROWS.add(new Row(o.get("output").getAsString(), o.get("catalyst").getAsString(), rs));
            }
        } catch (java.io.IOException | RuntimeException ex) {
            throw new IllegalStateException("laser drill ores unreadable", ex);
        }
    }

    private static List<String> strs(JsonObject o, String k) {
        List<String> l = new ArrayList<>();
        if (o.has(k)) o.getAsJsonArray(k).forEach(x -> l.add(x.getAsString()));
        return l;
    }

    private LaserDrillOres() {}

    public static List<Row> rows() { return ROWS; }
    public static boolean isCatalyst(String item) { return ROWS.stream().anyMatch(r -> r.catalyst().equals(item)); }
}
