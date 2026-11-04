package net.diaithir.mega;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.Reader;
import java.util.List;

/** One mega vehicle row from data/diaithirmega/catalog.json. Pure data, SI units (mass t, power MW, rate m3/s). */
public record MegaSpec(String id, String age, String clazz, double massT, double powerMW, String medium, double eta, double digM3s, int crew,
                       double deckM2, boolean autonomyCapable) {

    /** Specific excavation energy in MJ per m3, by medium. */
    public static double specificEnergy(String medium) {
        return switch (medium) {
            case "soil" -> 2.0;
            case "overburden" -> 6.0;
            case "ore" -> 25.0;
            case "rock" -> 40.0;
            case "regolith" -> 60.0;
            default -> throw new IllegalArgumentException("unknown medium " + medium);
        };
    }

    /** Dig rate in m3/s at full power: power x efficiency / specific energy. */
    public double maxRate() {
        return powerMW * eta / specificEnergy(medium);
    }

    public static List<MegaSpec> load(Reader r) {
        JsonObject root = new Gson().fromJson(r, JsonObject.class);
        return root.getAsJsonArray("vehicles").asList().stream().map(e -> {
            JsonObject o = e.getAsJsonObject();
            return new MegaSpec(o.get("id").getAsString(), o.get("age").getAsString(), o.get("class").getAsString(), o.get("mass_t").getAsDouble(),
                    o.get("power_MW").getAsDouble(), o.get("medium").getAsString(), o.get("eta").getAsDouble(), o.get("dig_m3_s").getAsDouble(),
                    o.get("crew").getAsInt(), o.get("deck_m2").getAsDouble(), o.get("autonomy_capable").getAsBoolean());
        }).toList();
    }
}
