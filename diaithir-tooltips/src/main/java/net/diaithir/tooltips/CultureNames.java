package net.diaithir.tooltips;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;

/** Per-culture name tables: assets/diaithirtooltips/cultures/&lt;slug&gt;.json = {"item": {id: name}, "block": {...}, "entity": {...}}. Client side. */
public final class CultureNames {
    private static final Map<String, String> NONE = Map.of();
    private static ResourceManager cacheOwner;
    private static final Map<String, JsonObject> CACHE = new HashMap<>();

    private CultureNames() {}

    /** The native name of a registry entry in the local player's starting culture, or null. kind is "item", "block" or "entity". */
    public static String name(String kind, String id) {
        var player = Minecraft.getInstance().player;
        if (player == null) {
            return null;
        }
        String slug = player.getData(DiaithirTooltips.CULTURE);
        var table = table(slug.isEmpty() ? DiaithirTooltips.DEFAULT_CULTURE : slug);
        if (table != null && table.has(kind)) {
            var v = table.getAsJsonObject(kind).get(id);
            return v == null ? null : v.getAsString();
        }
        return null;
    }

    private static JsonObject table(String slug) {
        var manager = Minecraft.getInstance().getResourceManager();
        if (manager != cacheOwner) {
            CACHE.clear();
            cacheOwner = manager;
        }
        return CACHE.computeIfAbsent(slug, s -> {
            var res = manager.getResource(Identifier.fromNamespaceAndPath(DiaithirTooltips.ID, "cultures/" + s + ".json"));
            if (res.isEmpty()) {
                return new JsonObject();
            }
            try (var in = new InputStreamReader(res.get().open(), StandardCharsets.UTF_8)) {
                return JsonParser.parseReader(in).getAsJsonObject();
            } catch (Exception e) {
                return new JsonObject();
            }
        });
    }
}
