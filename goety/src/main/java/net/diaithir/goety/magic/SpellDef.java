package net.diaithir.goety.magic;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.diaithir.goety.Spec;

/** One spell, bound to the focus item of the same id. Loaded from goety_spec/spells.json (written by tools/gen.py). */
public record SpellDef(String id, String type, int cost, int cast, int cooldown, String kind, JsonObject params, boolean hold, int interval) {
    private static Map<String, SpellDef> ALL;

    public static synchronized Map<String, SpellDef> all() {
        if (ALL == null) {
            Map<String, SpellDef> m = new LinkedHashMap<>();
            for (SpellDef d : Spec.read("spells.json", new TypeToken<List<SpellDef>>() {})) m.put(d.id(), d);
            ALL = m;
        }
        return ALL;
    }

    public static SpellDef get(String id) {
        return all().get(id);
    }

    public double d(String key, double def) {
        JsonElement e = params.get(key);
        return e == null ? def : e.getAsDouble();
    }

    public int i(String key, int def) {
        return (int) Math.round(d(key, def));
    }

    public String s(String key, String def) {
        JsonElement e = params.get(key);
        return e == null ? def : e.getAsString();
    }

    public List<String> list(String key) {
        List<String> out = new ArrayList<>();
        JsonElement e = params.get(key);
        if (e != null && e.isJsonArray()) for (JsonElement x : e.getAsJsonArray()) out.add(x.getAsString());
        return out;
    }

    /** Effects as {id, duration, amplifier}. */
    public List<Object[]> effects() {
        List<Object[]> out = new ArrayList<>();
        JsonElement e = params.get("effects");
        if (e != null && e.isJsonArray()) {
            for (JsonElement x : e.getAsJsonArray()) {
                JsonArray a = x.getAsJsonArray();
                out.add(new Object[] {a.get(0).getAsString(), a.get(1).getAsInt(), a.get(2).getAsInt()});
            }
        }
        return out;
    }
}
