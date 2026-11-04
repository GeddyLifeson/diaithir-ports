package org.diaithir.mega;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/** Dig rows of data/megamachines/catalog.json: rate (m3/s), medium, power and mass. Kept apart from MegaCatalog (cab rows). */
public final class MegaRates {
    public record Row(String id, double digM3s, String medium, double powerMW, double massT) {}

    private static final Map<String, Row> ROWS = new HashMap<>();

    static {
        try (var in = MegaRates.class.getResourceAsStream("/data/megamachines/catalog.json")) {
            JsonObject root = new Gson().fromJson(new InputStreamReader(in), JsonObject.class);
            for (var e : root.getAsJsonArray("vehicles")) {
                JsonObject o = e.getAsJsonObject();
                String id = o.get("id").getAsString();
                ROWS.put(id, new Row(id, o.get("dig_m3_s").getAsDouble(), o.get("medium").getAsString(), o.get("power_MW").getAsDouble(),
                        o.get("mass_t").getAsDouble()));
            }
        } catch (java.io.IOException | RuntimeException ex) {
            throw new IllegalStateException("megamachines catalog unreadable", ex);
        }
    }

    private MegaRates() {}

    public static Row get(String id) { return ROWS.get(id); }
}
