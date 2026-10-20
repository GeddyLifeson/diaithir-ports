package net.diaithir.compat;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/** data/diaithircompat/site_people.json and the MCA clothing it names (tools/gen_people.py). */
class SitePeopleDataTest {
    private static final Path RES = Path.of("src/main/resources");
    // metal, later weapons and made goods: nothing an Age I person could carry
    private static final Pattern LATER_AGE = Pattern.compile("iron|gold|copper|diamond|netherite|emerald|chain|crossbow|shield|trident|mace|lantern|book|paper|glass|brick|bread|map|compass|clock|bucket|shears|flint_and_steel|minecart|boat");
    private static final Set<String> SITE_TYPES = Set.of("hunter_camp", "shell_midden", "knapping_site", "rock_shelter", "pit_house",
            "ppnb_house", "mammoth_bone_dwelling", "natufian_house", "ppna_house", "gobekli_enclosure", "jericho_tower");

    private static JsonElement read(Path p) throws IOException {
        return JsonParser.parseString(Files.readString(p));
    }

    @Test
    void everyCultureHasAnAgeIKitAndClothing() throws IOException {
        JsonObject data = read(RES.resolve("data/diaithircompat/site_people.json")).getAsJsonObject();
        JsonObject kits = data.getAsJsonObject("kits"), cultures = data.getAsJsonObject("cultures");
        assertEquals(74, cultures.size());
        assertEquals(SITE_TYPES, data.getAsJsonObject("counts").keySet());
        for (Map.Entry<String, JsonElement> e : data.getAsJsonObject("counts").entrySet()) {
            JsonArray r = e.getValue().getAsJsonArray();
            assertTrue(1 <= r.get(0).getAsInt() && r.get(0).getAsInt() <= r.get(1).getAsInt(), e.getKey());
        }
        for (Map.Entry<String, JsonElement> e : cultures.entrySet()) {
            JsonObject c = e.getValue().getAsJsonObject();
            assertTrue(kits.has(c.get("kit").getAsString()), e.getKey());
            assertFalse(LATER_AGE.matcher(c.get("mainhand").getAsString()).find(), e.getKey());
            for (JsonElement i : c.getAsJsonArray("carried"))
                assertFalse(LATER_AGE.matcher(i.getAsJsonObject().get("item").getAsString()).find(), e.getKey());
            assertTrue(c.get("villager_type").getAsString().matches("minecraft:(snow|taiga|desert|savanna|plains|jungle|swamp)"), e.getKey());
        }
        for (Map.Entry<String, JsonElement> e : kits.entrySet()) {
            Set<String> listed = new HashSet<>();
            for (JsonElement entry : read(RES.resolve("data/diaithircompat/skins/clothing/" + e.getKey() + ".json")).getAsJsonArray()) {
                JsonObject o = entry.getAsJsonObject();
                assertEquals("diaithircompat:" + e.getKey(), o.get("profession").getAsString());
                listed.add(o.get("id").getAsString());
            }
            JsonObject clothing = e.getValue().getAsJsonObject().getAsJsonObject("clothing");
            for (String gender : new String[]{"male", "female"}) {
                assertFalse(clothing.getAsJsonArray(gender).isEmpty(), e.getKey() + " " + gender);
                for (JsonElement id : clothing.getAsJsonArray(gender)) {
                    String s = id.getAsString();
                    assertTrue(s.contains("/" + gender + "/") && listed.contains(s), s);
                    assertTrue(Files.isRegularFile(RES.resolve("assets/diaithircompat/" + s.substring("diaithircompat:".length()))), s);
                }
            }
        }
    }
}
