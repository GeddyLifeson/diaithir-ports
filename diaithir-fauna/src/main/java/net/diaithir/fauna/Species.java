package net.diaithir.fauna;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** One species: data only, the body is its plan. Loaded from data/diaithirfauna/species.json. */
public final class Species {
    public static final List<String> FLAGS;
    public static final List<String> HABS;
    public static final List<Species> ALL = new ArrayList<>();
    private static final Map<String, Species> BY_ID = new HashMap<>();
    private static final Map<String, List<Species>> BY_PLAN = new HashMap<>();

    public static final int F_FLIES = 0, F_FLIGHTLESS = 1, F_NOCTURNAL = 2, F_DIURNAL = 3, F_HERD = 4, F_PACK = 5, F_VENOMOUS = 6,
            F_SWIMS = 7, F_AGGRESSIVE = 8, F_TOXIC = 9, F_ARBOREAL = 10, F_BURROW = 11, F_MIGRATORY = 12, F_DOMESTICABLE = 13,
            F_SCAV = 14, F_FAST = 15;

    public final String id, plan, name, diet;
    public final float scale, kg;
    public final int flags, hab;
    /** AD year of extinction, or Integer.MAX_VALUE when alive today. */
    public final int ext;
    public final boolean fossil;
    /** Million years ago, from/to; fossils only. */
    public final float maFrom, maTo;
    /** latMin, latMax, lonMin, lonMax per box; empty means anywhere. */
    public final float[][] boxes;
    public final int index;

    static {
        try (Reader r = new InputStreamReader(Species.class.getResourceAsStream("/data/diaithirfauna/species.json"), StandardCharsets.UTF_8)) {
            JsonObject root = new Gson().fromJson(r, JsonObject.class);
            FLAGS = root.getAsJsonArray("flags").asList().stream().map(e -> e.getAsString()).toList();
            HABS = root.getAsJsonArray("habitats").asList().stream().map(e -> e.getAsString()).toList();
            for (var e : root.getAsJsonArray("species")) {
                Species s = new Species(e.getAsJsonObject(), ALL.size());
                ALL.add(s);
                BY_ID.put(s.id, s);
                BY_PLAN.computeIfAbsent(s.plan, k -> new ArrayList<>()).add(s);
            }
        } catch (Exception ex) {
            throw new IllegalStateException("species.json", ex);
        }
    }

    private Species(JsonObject o, int index) {
        this.index = index;
        id = o.get("id").getAsString();
        plan = o.get("plan").getAsString();
        name = o.get("name").getAsString();
        diet = o.get("diet").getAsString();
        scale = o.get("scale").getAsFloat();
        kg = o.get("kg").getAsFloat();
        flags = o.get("flags").getAsInt();
        hab = o.has("hab") ? o.get("hab").getAsInt() : 0;
        ext = o.has("ext") ? o.get("ext").getAsInt() : Integer.MAX_VALUE;
        fossil = o.has("fossil");
        maFrom = fossil ? o.getAsJsonArray("ma").get(0).getAsFloat() : 0;
        maTo = fossil ? o.getAsJsonArray("ma").get(1).getAsFloat() : 0;
        if (o.has("boxes")) {
            var a = o.getAsJsonArray("boxes");
            boxes = new float[a.size()][4];
            for (int i = 0; i < boxes.length; i++) for (int j = 0; j < 4; j++) boxes[i][j] = a.get(i).getAsJsonArray().get(j).getAsFloat();
        } else {
            boxes = new float[0][];
        }
    }

    public static Species byId(String id) { return BY_ID.get(id); }

    public static List<Species> ofPlan(String plan) { return BY_PLAN.getOrDefault(plan, List.of()); }

    public boolean has(int flag) { return (flags & (1 << flag)) != 0; }

    public boolean inRange(double lat, double lon) {
        if (boxes.length == 0) return true;
        for (float[] b : boxes) {
            if (lat < b[0] || lat > b[1]) continue;
            if (b[2] <= b[3] ? lon >= b[2] && lon <= b[3] : lon >= b[2] || lon <= b[3]) return true;
        }
        return false;
    }

    public boolean aliveAt(int adYear) { return !fossil && adYear < ext; }
}
