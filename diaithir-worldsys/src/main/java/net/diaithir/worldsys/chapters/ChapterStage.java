package net.diaithir.worldsys.chapters;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.Identifier;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * One stage as read from {@code data/<ns>/chapters/stages/<name>.json}. Format follows Chapters (gabinx, MIT):
 * {@code items} entries are an item id, {@code #tag} or {@code @namespace}; {@code recipes} and {@code dimensions}
 * are ids. Pure data, no Minecraft registry access, so it can be unit tested.
 */
public record ChapterStage(Identifier id, Set<String> items, Set<String> recipes, Set<String> dimensions) {

    public static ChapterStage fromJson(Identifier id, JsonObject json) {
        return new ChapterStage(id, strings(json, "items"), strings(json, "recipes"), strings(json, "dimensions"));
    }

    private static Set<String> strings(JsonObject json, String key) {
        Set<String> out = new LinkedHashSet<>();
        if (!json.has(key) || !json.get(key).isJsonArray()) return out;
        JsonArray arr = json.getAsJsonArray(key);
        for (JsonElement e : arr) {
            if (e.isJsonPrimitive()) out.add(e.getAsString().trim());
            else if (e.isJsonObject() && e.getAsJsonObject().has("id")) out.add(e.getAsJsonObject().get("id").getAsString().trim());
        }
        return out;
    }
}
