package net.diaithir.fauna;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** A body plan: one model and one entity type, shared by many species. Loaded from data/diaithirfauna/plans. */
public final class Plan {
    public static final Map<String, Plan> ALL = new LinkedHashMap<>();

    public final String name;
    public final JsonObject json;
    /** "land", "water" or "fly". */
    public final String cls, sound;
    public final float width, height;

    static {
        try {
            String[] names = new Gson().fromJson(read("/data/diaithirfauna/plans/index.json"), String[].class);
            for (String n : names) ALL.put(n, new Plan(n));
        } catch (Exception e) {
            throw new IllegalStateException("plans", e);
        }
    }

    private Plan(String name) {
        this.name = name;
        this.json = new Gson().fromJson(read("/data/diaithirfauna/plans/" + name + ".json"), JsonObject.class);
        JsonObject m = json.getAsJsonObject("meta");
        cls = m.get("cls").getAsString();
        sound = m.get("sound").getAsString();
        width = m.get("w").getAsFloat();
        height = m.get("h").getAsFloat();
    }

    private static Reader read(String path) {
        return new InputStreamReader(Plan.class.getResourceAsStream(path), StandardCharsets.UTF_8);
    }

    public JsonObject part(String n) { return json.getAsJsonObject("parts").getAsJsonObject(n); }

    public List<String> roots() { return strings(json.getAsJsonArray("roots")); }

    public List<String> children(String n) { return strings(part(n).getAsJsonArray("children")); }

    private static List<String> strings(com.google.gson.JsonArray a) {
        List<String> out = new ArrayList<>();
        a.forEach(e -> out.add(e.getAsString()));
        return out;
    }

    public boolean water() { return cls.equals("water"); }

    public boolean fly() { return cls.equals("fly"); }
}
