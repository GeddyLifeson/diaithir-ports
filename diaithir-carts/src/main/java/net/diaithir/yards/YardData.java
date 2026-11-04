package net.diaithir.yards;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Reads data/diaithircarts/yards.json (written by tools/gen_rolling_stock.py) and registers yards and parts. */
public final class YardData {
    public record Named(String id, String name, int age) {}
    public record Crane(String id, String name, double liftKg, double speed, int age) {}
    public record Controller(String id, String name, YardDef.Tier tier) {}

    public static final List<Named> PARTS = new ArrayList<>();
    public record Vehicle(String id, String name, int age, String category, String gauge, int crew, String engine, int rated) {}

    public static final List<Vehicle> VEHICLES = new ArrayList<>();
    public static final List<Crane> CRANES = new ArrayList<>();
    public static final List<Controller> CONTROLLERS = new ArrayList<>();

    private YardData() {}

    public static void load(String namespace) {
        JsonObject root;
        try (var in = YardData.class.getResourceAsStream("/data/" + namespace + "/yards.json")) {
            if (in == null) throw new IOException("yards.json missing");
            root = new Gson().fromJson(new InputStreamReader(in), JsonObject.class);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        for (JsonElement e : root.getAsJsonArray("parts")) {
            JsonObject o = e.getAsJsonObject();
            PartDef p = new PartDef(o.get("item").getAsString(), o.get("kind").getAsString(), o.get("mass_kg").getAsDouble(), o.get("hp").getAsDouble(),
                    o.get("power_w").getAsDouble(), 0, 0, 0, 0, o.get("min_age").getAsInt(), Map.of(), o.get("block").getAsString());
            YardRegistry.register(p);
            PARTS.add(new Named(path(p.item()), o.get("name").getAsString(), p.minAge()));
        }
        for (JsonElement e : root.getAsJsonArray("vehicles")) {
            JsonObject o = e.getAsJsonObject();
            VEHICLES.add(new Vehicle(o.get("id").getAsString(), o.get("name").getAsString(), o.get("age").getAsInt(), o.get("category").getAsString(),
                    o.has("gauge") ? o.get("gauge").getAsString() : "any", o.has("crew") ? o.get("crew").getAsInt() : 0, o.has("engine") ? o.get("engine").getAsString() : "", o.has("rated") ? o.get("rated").getAsInt() : 0));
        }
        for (JsonElement e : root.getAsJsonArray("yards")) {
            JsonObject o = e.getAsJsonObject();
            List<YardDef.Stage> stages = new ArrayList<>();
            for (JsonElement se : o.getAsJsonArray("stages")) {
                JsonObject s = se.getAsJsonObject();
                List<YardDef.Slot> slots = new ArrayList<>();
                for (JsonElement sl : s.getAsJsonArray("slots")) slots.add(new YardDef.Slot(sl.getAsJsonObject().get("kind").getAsString(), sl.getAsJsonObject().get("count").getAsInt()));
                stages.add(new YardDef.Stage(s.get("id").getAsString(), slots, s.get("base_ticks").getAsInt(), s.get("ghost").getAsString()));
            }
            JsonArray fp = o.getAsJsonArray("footprint");
            YardRegistry.register(new YardDef(o.get("id").getAsString(), YardDef.Tier.valueOf(o.get("tier").getAsString()), YardDef.Site.valueOf(o.get("site").getAsString()),
                    List.of(), o.get("result").getAsString(), stages, 1.0, new YardDef.Footprint(fp.get(0).getAsInt(), fp.get(1).getAsInt(), fp.get(2).getAsInt())));
        }
        for (JsonElement e : root.getAsJsonArray("cranes")) {
            JsonObject o = e.getAsJsonObject();
            CRANES.add(new Crane(o.get("id").getAsString(), o.get("name").getAsString(), o.get("lift_kg").getAsDouble(), o.get("speed").getAsDouble(), o.get("age").getAsInt()));
        }
        for (JsonElement e : root.getAsJsonArray("controllers")) {
            JsonObject o = e.getAsJsonObject();
            CONTROLLERS.add(new Controller(o.get("id").getAsString(), o.get("name").getAsString(), YardDef.Tier.valueOf(o.get("tier").getAsString())));
        }
    }

    private static String path(String id) {
        return id.substring(id.indexOf(':') + 1);
    }
}
