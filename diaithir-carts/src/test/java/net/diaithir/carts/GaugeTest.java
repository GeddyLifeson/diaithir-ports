package net.diaithir.carts;

import static org.junit.jupiter.api.Assertions.*;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class GaugeTest {
    @Test
    void gaugesDoNotInterconnect() {
        assertTrue(Gauge.runs(Gauge.STANDARD, Gauge.STANDARD));
        assertTrue(Gauge.runs(Gauge.NARROW, Gauge.NARROW));
        assertFalse(Gauge.runs(Gauge.STANDARD, Gauge.NARROW));
        assertFalse(Gauge.runs(Gauge.NARROW, Gauge.STANDARD));
        assertTrue(Gauge.runs(Gauge.STANDARD, Gauge.BOTH) && Gauge.runs(Gauge.NARROW, Gauge.BOTH));
        assertTrue(Gauge.runs(Gauge.ANY, Gauge.NARROW) && Gauge.runs(Gauge.NARROW, Gauge.ANY));
    }

    @Test
    void tradeOffs() {
        assertTrue(Tier.NARROW.maxSpeed < Tier.STEEL.maxSpeed && Tier.NARROW.maxSpeed < Tier.BRONZE.maxSpeed, "narrow is slower");
        assertTrue(Gauge.NARROW.curveSpeed > Gauge.STANDARD.curveSpeed, "narrow takes curves");
        assertTrue(Gauge.NARROW.gradeKeep > Gauge.STANDARD.gradeKeep, "narrow takes grades");
        assertTrue(Gauge.NARROW.payload < Gauge.STANDARD.payload, "narrow carries less");
        assertTrue(Gauge.NARROW.cost < Gauge.STANDARD.cost, "narrow is cheaper");
        assertEquals(Gauge.NARROW, Tier.NARROW.gauge);
        assertEquals(Gauge.BOTH, Tier.DUAL.gauge);
        assertEquals(Gauge.STANDARD, Tier.FAST.gauge);
    }

    @Test
    void rosterIsTwoGaugesWithinLimits() {
        JsonObject root = JsonParser.parseReader(new InputStreamReader(getClass().getResourceAsStream("/data/diaithircarts/yards.json"))).getAsJsonObject();
        JsonArray vs = root.getAsJsonArray("vehicles");
        Set<String> ids = new HashSet<>();
        int narrow = 0;
        for (JsonElement e : vs) {
            JsonObject v = e.getAsJsonObject();
            String id = v.get("id").getAsString(), g = v.get("gauge").getAsString();
            assertTrue(ids.add(id), "duplicate " + id);
            assertTrue(g.equals("standard") || g.equals("narrow"), id + " has gauge " + g);
            if (g.equals("narrow")) {
                narrow++;
                assertTrue(v.get("mass_kg").getAsInt() <= Gauge.NARROW.maxMassKg, id + " is too heavy for narrow gauge");
            }
            if (v.get("category").getAsString().equals("loco_steam")) assertEquals(2, v.get("crew").getAsInt(), id);
        }
        assertTrue(narrow > 20 && vs.size() > 400, "roster too small: " + vs.size() + " vehicles, " + narrow + " narrow");
    }
}
