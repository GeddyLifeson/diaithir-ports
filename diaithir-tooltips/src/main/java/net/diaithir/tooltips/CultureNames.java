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
        slug = slug.isEmpty() ? DiaithirTooltips.DEFAULT_CULTURE : slug;
        var table = table(slug);
        if (table.has(kind)) {
            var v = table.getAsJsonObject(kind).get(id);
            if (v != null) {
                return v.getAsString();
            }
        }
        return composed(slug, kind, id);
    }

    /** Other cultures: the concept phrase of the id composed in the culture's language (DiaithirRPG content/names, tools/names/README.md). */
    private static String composed(String slug, String kind, String id) {
        var units = data("names/units.json");
        var unit = units.has(slug) ? units.getAsJsonObject(slug) : null;
        if (unit == null || unit.get("lang").getAsString().equals("diaithiris")) {
            return null;
        }
        var items = data("names/items.json");
        var ids = items.getAsJsonObject("ids");
        var idx = ids.get(kind.equals("item") || kind.equals("block") ? id : kind + ":" + id);
        if (idx == null && kind.equals("block")) {
            return null;
        }
        if (idx == null) {
            return null;
        }
        var lang = data("names/langs/" + unit.get("lang").getAsString() + ".json");
        if (!lang.has("w")) {
            return null;
        }
        var w = lang.getAsJsonObject("w");
        var adj = adjectives(items);
        String link = lang.has("link") ? lang.get("link").getAsString() : "";
        boolean headFirst = lang.get("order").getAsString().equals("hf");
        boolean han = lang.has("script") && lang.get("script").getAsString().equals("han");
        String[] toks = items.getAsJsonArray("phrases").get(idx.getAsInt()).getAsString().split(" ");
        var parts = new java.util.ArrayList<String>();
        String head = toks[toks.length - 1];
        if (headFirst) {
            parts.add(word(w, head));
            for (int i = toks.length - 2; i >= 0; i--) {
                if (!link.isEmpty() && !adj.contains(toks[i])) {
                    parts.add(link);
                }
                parts.add(word(w, toks[i]));
            }
        } else {
            for (int i = 0; i < toks.length - 1; i++) {
                parts.add(word(w, toks[i]));
                if (!link.isEmpty() && !adj.contains(toks[i])) {
                    parts.add(link);
                }
            }
            parts.add(word(w, head));
        }
        for (String part : parts) {
            if (part == null) {
                return null;
            }
        }
        String out = String.join(han ? "" : " ", parts);
        return out.isEmpty() ? null : out.substring(0, out.offsetByCodePoints(0, 1)).toUpperCase() + out.substring(out.offsetByCodePoints(0, 1));
    }

    private static String word(JsonObject w, String concept) {
        if (ROMAN.contains(concept)) {
            return concept.toUpperCase();
        }
        var v = w.get(concept);
        return v == null ? null : v.getAsString();
    }

    private static final java.util.Set<String> ROMAN = java.util.Set.of("ii", "iii", "iv", "vi", "vii", "viii", "ix", "xi", "xii");
    private static java.util.Set<String> adjCache;

    private static java.util.Set<String> adjectives(JsonObject items) {
        if (adjCache == null) {
            adjCache = new java.util.HashSet<>();
            items.getAsJsonArray("adj").forEach(e -> adjCache.add(e.getAsString()));
        }
        return adjCache;
    }

    private static JsonObject table(String slug) {
        return data("cultures/" + slug + ".json");
    }

    /** A JSON resource under assets/diaithirtooltips/, parsed once per resource-manager generation; an empty object if absent. */
    private static JsonObject data(String path) {
        var manager = Minecraft.getInstance().getResourceManager();
        if (manager != cacheOwner) {
            CACHE.clear();
            adjCache = null;
            cacheOwner = manager;
        }
        return CACHE.computeIfAbsent(path, s -> {
            var res = manager.getResource(Identifier.fromNamespaceAndPath(DiaithirTooltips.ID, s));
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
