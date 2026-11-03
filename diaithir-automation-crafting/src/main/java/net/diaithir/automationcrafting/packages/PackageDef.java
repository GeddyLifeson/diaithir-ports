package net.diaithir.automationcrafting.packages;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * One bulk recipe. Port of The Package Mod's CraftingPackage (whizzball1, LGPL-3.0): a package turns a pile of inputs into a
 * crate that unpacks into the result, and must be researched first. Read from datapack JSON instead of packageadder.json.
 */
public record PackageDef(String id, String name, Req result, List<Req> inputs, List<Req> researchItems, List<Req> researchPacks, List<String> prereqs) {
    /** An item id (or, for research packs, a package id) and how many. */
    public record Req(String item, int count) {}

    /** Parses one file. Returns null and reports through warn when the file is unusable. */
    public static PackageDef parse(String id, JsonElement root, Consumer<String> warn) {
        if (!root.isJsonObject()) {
            warn.accept("not an object");
            return null;
        }
        JsonObject o = root.getAsJsonObject();
        Req result = req(o.get("result"), warn);
        List<Req> inputs = reqs(o.get("inputs"), warn);
        if (result == null || inputs.isEmpty()) {
            warn.accept("needs a result and at least one input");
            return null;
        }
        List<Req> items = new ArrayList<>();
        List<Req> packs = new ArrayList<>();
        List<String> pre = new ArrayList<>();
        if (o.has("research") && o.get("research").isJsonObject()) {
            JsonObject r = o.getAsJsonObject("research");
            items = reqs(r.get("items"), warn);
            packs = reqs(r.get("packs"), warn);
            if (r.has("prereqs") && r.get("prereqs").isJsonArray()) {
                for (JsonElement e : r.getAsJsonArray("prereqs")) pre.add(e.getAsString());
            }
        }
        String name = o.has("name") ? o.get("name").getAsString() : id;
        return new PackageDef(id, name, result, inputs, items, packs, pre);
    }

    private static Req req(JsonElement e, Consumer<String> warn) {
        if (e == null || !e.isJsonObject() || !e.getAsJsonObject().has("item")) {
            warn.accept("bad entry " + e);
            return null;
        }
        JsonObject o = e.getAsJsonObject();
        int n = o.has("count") ? o.get("count").getAsInt() : 1;
        return n < 1 ? null : new Req(o.get("item").getAsString(), n);
    }

    private static List<Req> reqs(JsonElement e, Consumer<String> warn) {
        List<Req> out = new ArrayList<>();
        if (e != null && e.isJsonArray()) {
            JsonArray a = e.getAsJsonArray();
            for (JsonElement x : a) {
                Req r = req(x, warn);
                if (r != null) out.add(r);
            }
        }
        return out;
    }
}
