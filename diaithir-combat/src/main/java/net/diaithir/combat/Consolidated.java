package net.diaithir.combat;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Loads the clean-room content bundles under data/diaithircombat/consolidated: item counts and the old-id to new-id migration map. */
public final class Consolidated {
    private static final Logger LOG = LoggerFactory.getLogger("diaithircombat/consolidated");
    private static final String BASE = "/data/diaithircombat/consolidated/";
    /** old mod id:path to new id, merged from every bundle (ids never change; old ids alias to the new ones). */
    public static final Map<String, String> ID_MIGRATION = new TreeMap<>();
    public static int items;
    public static int bundles;

    private Consolidated() {}

    public static void load() {
        Gson gson = new Gson();
        String[] names = read(gson, "index.json", String[].class);
        if (names == null) {
            return;
        }
        for (String n : names) {
            JsonObject o = read(gson, n, JsonObject.class);
            if (o == null) {
                continue;
            }
            bundles++;
            if (o.has("items") && o.get("items").isJsonArray()) {
                items += o.getAsJsonArray("items").size();
            }
            if (o.has("id_migration") && o.get("id_migration").isJsonObject()) {
                o.getAsJsonObject("id_migration").entrySet().forEach(e -> ID_MIGRATION.put(e.getKey(), e.getValue().getAsString()));
            }
        }
        LOG.info("{} bundles, {} items, {} id migrations", bundles, items, ID_MIGRATION.size());
    }

    private static <T> T read(Gson gson, String path, Class<T> type) {
        try (var in = Consolidated.class.getResourceAsStream(BASE + path)) {
            return in == null ? null : gson.fromJson(new InputStreamReader(in, java.nio.charset.StandardCharsets.UTF_8), type);
        } catch (Exception e) {
            LOG.warn("bad bundle {}: {}", path, e.toString());
            return null;
        }
    }
}
