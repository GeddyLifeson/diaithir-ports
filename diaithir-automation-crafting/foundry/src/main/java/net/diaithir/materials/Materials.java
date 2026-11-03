package net.diaithir.materials;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** The shared property table for the building-material classes, read once from data/diaithir_materials/materials.json. */
public final class Materials {
    public static final List<String> CLASSES = List.of("timber", "masonry", "concrete", "steel", "glass", "lining", "roofing", "road", "insulation");
    public static final List<String> AGES = List.of("I", "II", "III", "IV", "V", "VI", "VII", "VIII", "RODOS");

    /** One stage of one class. Units: kg/m3, MPa, W/(m K), minutes; hardness and resistance are block strength numbers. */
    public record Row(String id, String cls, String age, double density, double compressive, double tensile, double conductivity, double fireMin,
                      float hardness, float resistance, boolean built) {}

    private static final Map<String, Row> ROWS = new LinkedHashMap<>();
    static {
        try (Reader r = new InputStreamReader(Materials.class.getResourceAsStream("/data/diaithir_materials/materials.json"), StandardCharsets.UTF_8)) {
            for (JsonElement e : JsonParser.parseReader(r).getAsJsonObject().getAsJsonArray("rows")) {
                JsonObject o = e.getAsJsonObject();
                Row row = new Row(o.get("id").getAsString(), o.get("class").getAsString(), o.get("age").getAsString(), o.get("density_kg_m3").getAsDouble(),
                        o.get("compressive_mpa").getAsDouble(), o.get("tensile_mpa").getAsDouble(), o.get("conductivity_w_mk").getAsDouble(),
                        o.get("fire_min").getAsDouble(), o.get("hardness").getAsFloat(), o.get("resistance").getAsFloat(), o.get("built").getAsBoolean());
                ROWS.put(row.id(), row);
            }
        } catch (IOException | RuntimeException e) {
            throw new IllegalStateException("cannot read materials.json", e);
        }
    }

    private Materials() {}

    public static Row get(String id) {
        Row r = ROWS.get(id);
        if (r == null) throw new IllegalArgumentException("no material " + id);
        return r;
    }

    public static Collection<Row> all() { return ROWS.values(); }
}
