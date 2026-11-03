package net.diaithir.worldsys.economy;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Territory: a culture's prices apply only inside its claim, the discs around its settlements. Port of econClaims/econClaimAt (01_polity.js). */
public final class EconClaims {
    private EconClaims() {}

    public static final double CLAIM_BASE = 160;
    public static final Map<String, Double> TIER = Map.of("band", 1.0, "town", 1.0, "county", 1.5, "kingdom", 2.0, "country", 3.0);

    public record Claim(double x, double z, long r, String culture, String realm, String town, String name) {}

    public static String slug(String s) {
        String t = (s == null ? "" : s.toLowerCase(Locale.ROOT)).replaceAll("[^a-z0-9]+", "_").replaceAll("^_+|_+$", "");
        return t.isEmpty() ? "free" : t;
    }

    private static String str(JsonObject o, String k) { return o != null && o.has(k) && !o.get(k).isJsonNull() ? o.get(k).getAsString() : null; }

    /** sim: the engine's WorldSim as JSON ({settlements, factions}). One claim per standing settlement. */
    public static List<Claim> fromSim(JsonObject sim) {
        List<Claim> out = new ArrayList<>();
        JsonObject factions = sim.getAsJsonObject("factions");
        for (Map.Entry<String, JsonElement> e : sim.getAsJsonObject("settlements").entrySet()) {
            JsonObject s = e.getValue().getAsJsonObject();
            if ("ruined".equals(str(s, "state"))) continue;
            JsonObject f = factions.has(str(s, "faction")) ? factions.getAsJsonObject(str(s, "faction")) : null;
            JsonObject root = f;
            int n = 0;
            while (root != null && str(root, "parent") != null && factions.has(str(root, "parent")) && n++ < 8) root = factions.getAsJsonObject(str(root, "parent"));
            String sc = str(s, "culture");
            String culture = slug(sc != null && !sc.isEmpty() ? sc : str(f, "culture"));
            double pop = s.has("population") && !s.get("population").isJsonNull() && s.get("population").getAsDouble() != 0 ? s.get("population").getAsDouble() : 1;
            String tier = root != null ? str(root, "tier") : "town";
            double r = (CLAIM_BASE + 8 * Math.sqrt(Math.max(1, pop))) * TIER.getOrDefault(tier == null ? "town" : tier, 1.0);
            out.add(new Claim(s.get("x").getAsDouble(), s.get("z").getAsDouble(), Math.round(r), culture, root != null ? str(root, "id") : str(s, "faction"), e.getKey(), str(s, "name")));
        }
        return out;
    }

    /** The claim covering (x, z), or null in the wilds. Where claims overlap, the one the point sits deepest inside (distance over radius) wins. */
    public static Claim at(List<Claim> claims, double x, double z) {
        Claim best = null;
        double bd = 1;
        for (Claim c : claims) {
            double d = Math.hypot(c.x() - x, c.z() - z) / c.r();
            if (d <= bd) { bd = d; best = c; }
        }
        return best;
    }
}
