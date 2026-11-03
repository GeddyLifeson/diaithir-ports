package net.diaithir.firearms;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** guns.json (written by tools/gen.py). */
public final class Defs {
    public record Gun(String id, @SerializedName("class") String cls, String ammo, float damage, int cooldown, int mag, int reload,
                      float spread, int range, int pellets, boolean auto, String age, float recoil) {
        public int ageNo() { return Defs.ageNo(age); }
    }
    public record Att(String id, String kind, float value) {}
    record File(List<Gun> guns, List<Att> attachments) {}

    public static final Map<String, Gun> GUNS = new LinkedHashMap<>();
    public static final Map<String, Att> ATTS = new LinkedHashMap<>();

    static {
        try (var r = new InputStreamReader(Defs.class.getResourceAsStream("/guns.json"))) {
            File f = new Gson().fromJson(r, File.class);
            f.guns().forEach(g -> GUNS.put(g.id(), g));
            f.attachments().forEach(a -> ATTS.put(a.id(), a));
        } catch (Exception e) {
            throw new IllegalStateException("guns.json missing", e);
        }
    }

    public static int ageNo(String a) {
        return switch (a) { case "I" -> 1; case "II" -> 2; case "III" -> 3; case "IV" -> 4; case "V" -> 5; case "VI" -> 6; case "VII" -> 7; default -> 8; };
    }

    private Defs() {}
}
