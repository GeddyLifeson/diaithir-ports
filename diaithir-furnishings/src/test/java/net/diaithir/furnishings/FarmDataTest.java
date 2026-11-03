package net.diaithir.furnishings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class FarmDataTest {
    private static final List<String> AGES = List.of("I", "II", "III", "IV", "V", "VI", "VII");

    @Test
    void everyModernMachineHasPrimitivePredecessors() throws Exception {
        Map<String, String> pred = new HashMap<>(), mode = new HashMap<>();
        for (String l : Files.readAllLines(Path.of("roster/farm_mechanics.csv")).subList(1, Files.readAllLines(Path.of("roster/farm_mechanics.csv")).size())) {
            String[] c = l.split(",", -1);
            pred.put(c[0], c[5]);
            mode.put(c[0], c[4]);
        }
        com.google.gson.JsonObject gates = com.google.gson.JsonParser.parseString(Files.readString(Path.of("age_gates.json"))).getAsJsonObject().getAsJsonObject("items");
        for (String id : pred.keySet()) {
            String age = gates.get("diaithirfurnishings:" + id).getAsString();
            if (AGES.indexOf(age) < 4) continue;  // Ages I-IV are the primitive end
            String cur = id;
            int hops = 0;
            while (AGES.indexOf(gates.get("diaithirfurnishings:" + cur).getAsString()) >= 4) {
                cur = pred.get(cur);
                assertTrue(cur != null && !cur.isEmpty(), id + " has no primitive predecessor chain");
                assertTrue(++hops < 12, id + " chain loops");
            }
        }
    }

    @Test
    void farmMachinesAreNotCutOrCrafted() throws Exception {
        for (String l : Files.readAllLines(Path.of("roster/roster.csv"))) {
            if (!l.contains(",farm_machine,")) continue;
            String id = l.substring(0, l.indexOf(','));
            assertTrue(!Files.exists(Path.of("src/main/resources/data/diaithirfurnishings/recipe/everyday/" + id + ".json")), id + " has a stonecutting recipe");
            assertTrue(l.contains("https://en.wikipedia.org/wiki/"), id + " has no Wikipedia source");
        }
    }

    @Test
    void swathAndEnergy() {
        assertEquals(6 * 10, FarmMath.swath(6, 10).size());
        assertEquals(1, FarmMath.swath(1, 1).size());
        assertEquals(150_000 * 10 / 2, FarmMath.swathJoules(150_000, "powered", 1.0));
        assertEquals(2 * FarmMath.swathJoules(30_000, "powered", 1.0), FarmMath.swathJoules(30_000, "powered", 0.5));
        assertEquals(24_000, FarmMath.tankPoints(24_000_000L));
    }
}
