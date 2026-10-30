package net.diaithir.activity;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The data-driven side: what activities exist and which tools, blocks, stations and menus mean which activity.
 * Several JSON documents are merged, lowest priority first. A later document wins: its rules are tried first
 * and its activity definitions replace earlier ones. A null activity removes it.
 *
 * Rule keys name a thing by id ("minecraft:bow") or by tag with a leading hash ("#minecraft:pickaxes").
 */
public final class ActivityData {
    public record Def(String id, String atlas, String sprite, String label, int priority, int hold) {}

    public record Rule(String match, String activity) {}

    /** The rule sections, in the order they appear in the JSON. */
    public static final List<String> SECTIONS = List.of("breaks", "tools", "stations", "menus", "uses");

    private final Map<String, Def> defs = new LinkedHashMap<>();
    private final Map<String, List<Rule>> rules = new LinkedHashMap<>();

    private ActivityData() {
        for (String s : SECTIONS) rules.put(s, new ArrayList<>());
    }

    public static ActivityData merge(List<JsonObject> docs) {
        ActivityData out = new ActivityData();
        for (JsonObject doc : docs) {
            if (doc.has("activities") && doc.get("activities").isJsonObject()) {
                for (Map.Entry<String, JsonElement> e : doc.getAsJsonObject("activities").entrySet()) {
                    if (e.getValue().isJsonNull()) {
                        out.defs.remove(e.getKey());
                    } else {
                        out.defs.put(e.getKey(), parseDef(e.getKey(), e.getValue().getAsJsonObject()));
                    }
                }
            }
            for (String section : SECTIONS) {
                if (!doc.has(section) || !doc.get(section).isJsonArray()) continue;
                List<Rule> add = new ArrayList<>();
                for (JsonElement el : doc.getAsJsonArray(section)) {
                    JsonObject o = el.getAsJsonObject();
                    add.add(new Rule(o.get("match").getAsString(), o.get("activity").getAsString()));
                }
                // newer documents are consulted first
                out.rules.get(section).addAll(0, add);
            }
        }
        return out;
    }

    private static Def parseDef(String id, JsonObject o) {
        return new Def(id,
            o.has("atlas") ? o.get("atlas").getAsString() : "minecraft:items",
            o.get("sprite").getAsString(),
            o.has("label") ? o.get("label").getAsString() : "activity.diaithiractivity." + id,
            o.has("priority") ? o.get("priority").getAsInt() : 40,
            o.has("hold") ? o.get("hold").getAsInt() : 60);
    }

    public Map<String, Def> defs() {
        return defs;
    }

    public List<Rule> rules(String section) {
        return rules.get(section);
    }

    /** First rule whose match is one of the keys (the thing's id and its "#tag" names) and whose activity still exists. */
    public String resolve(String section, Collection<String> keys) {
        for (Rule r : rules.get(section)) {
            if (keys.contains(r.match()) && defs.containsKey(r.activity())) return r.activity();
        }
        return null;
    }
}
