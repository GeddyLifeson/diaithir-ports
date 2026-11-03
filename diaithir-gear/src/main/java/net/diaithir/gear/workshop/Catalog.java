package net.diaithir.gear.workshop;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import net.diaithir.gear.DiaithirGear;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.loading.FMLPaths;

/**
 * Materials and blueprints. Bundled files are listed in data/diaithirgear/workshop/index.json
 * ({"materials": [...], "blueprints": [...]}, each entry a file in the same folder holding a JSON array).
 * Anything in config/diaithirgear/workshop/materials and /blueprints (*.json arrays) is added or replaces by id,
 * so the roster threads add their items by dropping a blueprint file in the bundled folder and listing it.
 */
public final class Catalog {
    private static final Map<String, Craft.Material> MATERIALS = new LinkedHashMap<>();
    private static final Map<String, Craft.Blueprint> BLUEPRINTS = new LinkedHashMap<>();
    public record Improvement(String id, String item, double damage, int durability, String name) {}

    private static final Map<String, Improvement> IMPROVEMENTS = new LinkedHashMap<>();
    private static final Gson GSON = new Gson();

    private Catalog() {}

    public static Map<String, Craft.Material> materials() { return MATERIALS; }

    public static Map<String, Improvement> improvements() { return IMPROVEMENTS; }

    public static Map<String, Craft.Blueprint> blueprints() { return BLUEPRINTS; }

    public static List<Craft.Blueprint> forStation(String station) {
        return BLUEPRINTS.values().stream().filter(b -> b.station().equals(station) && baseItem(b) != null).toList();
    }

    /** The result item of a blueprint, or null when that item is not in the registry (its mod is absent). */
    public static Item baseItem(Craft.Blueprint b) {
        return BuiltInRegistries.ITEM.get(Identifier.parse(b.base())).map(h -> h.value()).orElse(null);
    }

    /** Material for a stack in the given role, or null. Items may be ids or #tags. */
    public static Craft.Material materialFor(ItemStack st, String role) {
        for (Craft.Material m : MATERIALS.values()) {
            if (!m.roles().contains(role)) continue;
            for (String it : m.items()) {
                if (it.startsWith("#")) {
                    var tag = net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM, Identifier.parse(it.substring(1)));
                    if (st.is(tag)) return m;
                } else if (BuiltInRegistries.ITEM.getKey(st.getItem()).toString().equals(it)) return m;
            }
        }
        return null;
    }

    public static void load() {
        MATERIALS.clear();
        BLUEPRINTS.clear();
        IMPROVEMENTS.clear();
        String base = "/data/" + DiaithirGear.ID + "/workshop/";
        try (Reader r = new InputStreamReader(Catalog.class.getResourceAsStream(base + "index.json"))) {
            JsonObject idx = GSON.fromJson(r, JsonObject.class);
            for (var f : idx.getAsJsonArray("materials")) readMaterials(open(base + f.getAsString()));
            for (var f : idx.getAsJsonArray("improvements")) for (var e : open(base + f.getAsString())) { JsonObject o = e.getAsJsonObject(); IMPROVEMENTS.put(o.get("id").getAsString(), GSON.fromJson(o, Improvement.class)); }
            for (var f : idx.getAsJsonArray("blueprints")) readBlueprints(open(base + f.getAsString()));
        } catch (Exception ex) {
            throw new IllegalStateException("diaithirgear workshop index", ex);
        }
        Path cfg = FMLPaths.CONFIGDIR.get().resolve("diaithirgear/workshop");
        overlay(cfg.resolve("materials"), true);
        overlay(cfg.resolve("blueprints"), false);
        DiaithirGear.LOG.info("workshop: {} materials, {} blueprints", MATERIALS.size(), BLUEPRINTS.size());
    }

    private static JsonArray open(String path) throws Exception {
        try (Reader r = new InputStreamReader(Catalog.class.getResourceAsStream(path))) { return GSON.fromJson(r, JsonArray.class); }
    }

    private static void overlay(Path dir, boolean materials) {
        if (!Files.isDirectory(dir)) return;
        try (Stream<Path> s = Files.list(dir)) {
            for (Path p : s.filter(x -> x.toString().endsWith(".json")).sorted().toList()) {
                try (Reader r = Files.newBufferedReader(p)) {
                    JsonArray a = GSON.fromJson(r, JsonArray.class);
                    if (materials) readMaterials(a); else readBlueprints(a);
                }
            }
        } catch (Exception ex) {
            DiaithirGear.LOG.warn("workshop config {}: {}", dir, ex.toString());
        }
    }

    private static List<String> strings(JsonObject o, String key) {
        List<String> l = new ArrayList<>();
        if (o.has(key)) for (var e : o.getAsJsonArray(key)) l.add(e.getAsString());
        return List.copyOf(l);
    }

    static void readMaterials(JsonArray a) {
        for (var e : a) {
            JsonObject o = e.getAsJsonObject();
            String id = o.get("id").getAsString();
            MATERIALS.put(id, new Craft.Material(id, o.get("age").getAsInt(), o.get("hardness").getAsDouble(),
                    o.get("toughness").getAsDouble(), o.get("density").getAsDouble(), o.get("flex").getAsDouble(), o.get("color").getAsInt(),
                    strings(o, "roles"), strings(o, "items")));
        }
    }

    static void readBlueprints(JsonArray a) {
        for (var e : a) {
            JsonObject o = e.getAsJsonObject();
            String id = o.get("id").getAsString();
            List<Craft.Slot> slots = new ArrayList<>();
            for (var se : o.getAsJsonArray("slots")) {
                JsonObject s = se.getAsJsonObject();
                slots.add(new Craft.Slot(s.get("name").getAsString(), s.get("role").getAsString(), s.get("count").getAsInt(),
                        s.get("litres").getAsDouble(), s.get("damage").getAsDouble(), s.get("durability").getAsDouble(), s.get("speed").getAsDouble()));
            }
            BLUEPRINTS.put(id, new Craft.Blueprint(id, o.get("station").getAsString(), o.get("base").getAsString(), o.get("age").getAsInt(),
                    o.get("kind").getAsString(), o.get("damage").getAsDouble(), o.get("speed").getAsDouble(), o.get("durability").getAsInt(),
                    o.get("ref_mass_kg").getAsDouble(), o.has("improvements") ? o.get("improvements").getAsInt() : 2, o.has("schematic") && o.get("schematic").getAsBoolean(), o.has("model") ? o.get("model").getAsString() : id, List.copyOf(slots)));
        }
    }
}
