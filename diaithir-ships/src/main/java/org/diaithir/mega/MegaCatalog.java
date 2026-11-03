package org.diaithir.mega;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/** Vehicle rows of data/megamachines/catalog.json that the cab needs: autonomy capability and crew seats. */
public final class MegaCatalog {
    public record Row(String id, boolean autonomyCapable, int crew) {}

    private static final Map<String, Row> ROWS = new HashMap<>();

    static {
        try (var in = MegaCatalog.class.getResourceAsStream("/data/megamachines/catalog.json")) {
            JsonObject root = new Gson().fromJson(new InputStreamReader(in), JsonObject.class);
            for (var e : root.getAsJsonArray("vehicles")) {
                JsonObject o = e.getAsJsonObject();
                String id = o.get("id").getAsString();
                ROWS.put(id, new Row(id, o.get("autonomy_capable").getAsBoolean(), o.get("crew").getAsInt()));
            }
        } catch (java.io.IOException | RuntimeException ex) {
            throw new IllegalStateException("megamachines catalog unreadable", ex);
        }
    }

    private MegaCatalog() {}

    public static Row get(String id) { return ROWS.get(id); }
    public static java.util.Set<String> ids() { return ROWS.keySet(); }
}
