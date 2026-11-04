package org.diaithir.mega.site;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Staged-construction blueprints of the megastructures: data/megamachines/blueprints.json, made by Global-Repo S6917. */
public final class Blueprints {
    public static final List<String> AGES = List.of("I", "II", "III", "IV", "V", "VI", "VII", "VIII", "RODOS");

    public enum Kind { DIG, PLACE, REMOTE }

    /** amount is m3 for dig, tonnes otherwise; rate is per builder per second (remote: the shared launch budget). */
    public record Stage(String key, Kind kind, double amount, double rate, int fleet, int blocks, double functionShare, Map<String, Double> materials) {
        /** Tonnes of tag needed per unit of work. */
        public double perUnit(String tag) { return materials.get(tag) / amount; }
    }

    public record Structure(String id, String stationAge, double powerMW, int footprintCap, List<Stage> stages) {
        public int totalBlocks() { int n = 0; for (Stage s : stages) n += s.blocks(); return n; }
    }

    private static final Map<String, Structure> ALL = new LinkedHashMap<>();

    static {
        try (var in = Blueprints.class.getResourceAsStream("/data/megamachines/blueprints.json")) {
            JsonObject root = new Gson().fromJson(new InputStreamReader(in), JsonObject.class);
            for (var e : root.entrySet()) {
                JsonObject o = e.getValue().getAsJsonObject();
                List<Stage> stages = new ArrayList<>();
                for (var se : o.getAsJsonArray("stages")) {
                    JsonObject s = se.getAsJsonObject();
                    Map<String, Double> mats = new LinkedHashMap<>();
                    for (var m : s.getAsJsonObject("materials").entrySet()) mats.put(m.getKey(), m.getValue().getAsDouble());
                    stages.add(new Stage(s.get("key").getAsString(), Kind.valueOf(s.get("kind").getAsString().toUpperCase()),
                            s.get("amount").getAsDouble(), s.get("rate_per_s").getAsDouble(), s.get("fleet").getAsInt(),
                            s.get("blocks").getAsInt(), s.get("function_share").getAsDouble(), mats));
                }
                ALL.put(o.get("id").getAsString(), new Structure(o.get("id").getAsString(), o.get("station_age").getAsString(),
                        o.get("power_MW").getAsDouble(), 24, stages));
            }
        } catch (java.io.IOException | RuntimeException ex) {
            throw new IllegalStateException("megamachines blueprints unreadable", ex);
        }
    }

    private Blueprints() {}

    public static Structure get(String id) { return ALL.get(id); }
    public static java.util.Set<String> ids() { return ALL.keySet(); }
    public static java.util.Set<String> allTags() {
        java.util.Set<String> t = new java.util.TreeSet<>();
        for (Structure s : ALL.values()) for (Stage st : s.stages()) t.addAll(st.materials().keySet());
        return t;
    }
    public static int ageIndex(String age) { return AGES.indexOf(age); }
}
