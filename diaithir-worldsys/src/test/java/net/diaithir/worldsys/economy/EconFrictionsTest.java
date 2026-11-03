package net.diaithir.worldsys.economy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Spoilage, wear, seasons and the exchange, replayed against tools/economy/golden_frictions.js. */
class EconFrictionsTest {
    private static final double E = 1e-9;
    private static final Map<String, EconModel.Good> GOODS = Map.of("minecraft:beef", new EconModel.Good(3, 100), "minecraft:wheat", new EconModel.Good(1, 500),
        "minecraft:iron_ingot", new EconModel.Good(4, 200), "minecraft:leather", new EconModel.Good(2, 100), "minecraft:bread", new EconModel.Good(2, 100),
        "minecraft:salted_beef", new EconModel.Good(4, 50));

    private static JsonObject golden() {
        return JsonParser.parseReader(new InputStreamReader(EconFrictionsTest.class.getResourceAsStream("/econ_frictions_golden.json"))).getAsJsonObject();
    }

    private static void same(JsonObject want, Map<String, Double> got) {
        assertEquals(want.size(), got.size());
        want.entrySet().forEach(e -> assertEquals(e.getValue().getAsDouble(), got.get(e.getKey()), E, e.getKey()));
    }

    @Test
    void spoilage() {
        JsonObject g = golden();
        EconState st = new EconState();
        st.market.held("a").putAll(Map.of("minecraft:beef", 400.0, "minecraft:wheat", 800.0, "minecraft:iron_ingot", 150.0, "minecraft:leather", 300.0, "minecraft:bread", 90.0, "minecraft:salted_beef", 200.0));
        st.market.held("b").putAll(Map.of("minecraft:beef", 120.0, "minecraft:wheat", 2000.0));
        JsonArray rec = g.getAsJsonArray("spoil");
        for (int i = 0; i < 4; i++) {
            double lost = EconFrictions.spoil(st, GOODS::get, r -> r.equals("a") ? 1.0 : 0.4, null);
            assertEquals(rec.get(i).getAsJsonObject().get("lost").getAsDouble(), lost, E, "lost " + i);
            same(rec.get(i).getAsJsonObject().getAsJsonObject("a"), st.market.stock.get("a"));
            same(rec.get(i).getAsJsonObject().getAsJsonObject("b"), st.market.stock.get("b"));
        }
        String[] ids = {"minecraft:beef", "minecraft:leather", "minecraft:bread", "minecraft:salted_beef", "minecraft:egg", "minecraft:dried_kelp", "minecraft:stone", "diaithir_drinks:ale"};
        for (int i = 0; i < ids.length; i++) assertEquals(g.getAsJsonArray("rates").get(i).getAsDouble(), EconFrictions.spoilRate(ids[i], null), E, ids[i]);
        Double[] temps = {null, -5.0, 0.0, 4.0, 10.0, 25.0};
        for (int i = 0; i < temps.length; i++) assertEquals(g.getAsJsonArray("warmth").get(i).getAsDouble(), EconFrictions.warmth(temps[i]), E);
        double[] days = {0, 3, 14, 60};
        for (int i = 0; i < days.length; i++) assertEquals(g.getAsJsonArray("drink").get(i).getAsDouble(), EconFrictions.drinkRate(days[i]), E);
    }

    @Test
    void wearAndVelocity() {
        JsonObject g = golden();
        EconState st = new EconState();
        st.cur("x").treasury = 1; st.cur("x").money = 100000;
        for (int i = 0; i < 6; i++) {
            if (i == 3) st.cur("x").treasury = 100;
            JsonObject o = g.getAsJsonArray("wear").get(i).getAsJsonObject();
            assertEquals(o.get("paid").getAsDouble(), EconFrictions.wear(st, "x", "d"), E, "paid " + i);
            assertEquals(o.get("debase").getAsDouble(), st.des("d").debase, E);
            assertEquals(o.get("worn").getAsDouble(), st.des("d").worn, E);
            assertEquals(o.get("treasury").getAsDouble(), st.cur("x").treasury, E);
        }
        st.cur("x").vol = 500;
        assertEquals(g.get("velocity").getAsDouble(), EconFrictions.velocity(st.cur("x")), E);
    }

    @Test
    void seasons() {
        JsonArray rec = golden().getAsJsonArray("season");
        EconState st = new EconState();
        st.market.held("a").putAll(Map.of("minecraft:wheat", 500.0, "minecraft:iron_ingot", 200.0, "minecraft:bread", 100.0));
        st.market.held("b").putAll(Map.of("minecraft:wheat", 500.0));
        for (int w = 0; w < 14; w++) {
            EconFrictions.season(st, GOODS::get, w * 4L, r -> r.equals("b") ? 0.1 : null);
            JsonObject o = rec.get(w).getAsJsonObject();
            assertEquals(o.get("a").getAsDouble(), st.market.stock.get("a").get("minecraft:wheat"), E, "a " + w);
            assertEquals(o.get("b").getAsDouble(), st.market.stock.get("b").get("minecraft:wheat"), E, "b " + w);
            assertEquals(o.get("iron").getAsDouble(), st.market.stock.get("a").get("minecraft:iron_ingot"), E);
            assertEquals(o.get("bread").getAsDouble(), st.market.stock.get("a").get("minecraft:bread"), E);
        }
    }

    @Test
    void exchange() {
        JsonArray rec = golden().getAsJsonArray("exchange");
        EconState st = new EconState();
        EconState.ShareCompany co = EconExchange.charter(st, "x", "Kompunaid X");
        JsonObject o = rec.get(0).getAsJsonObject();
        assertEquals(o.get("price").getAsDouble(), EconExchange.price(co), E);
        assertEquals(o.get("cost10").getAsDouble(), EconExchange.cost(co, 10), E);
        assertEquals(o.get("costM").getAsDouble(), EconExchange.cost(co, -10), E);
        co.z = 120; co.fair = 140;
        o = rec.get(1).getAsJsonObject();
        assertEquals(o.get("price").getAsDouble(), EconExchange.price(co), E);
        assertEquals(o.get("cost50").getAsDouble(), EconExchange.cost(co, 50), E);
        assertEquals(o.get("costM").getAsDouble(), EconExchange.cost(co, -50), E);
        st.holdings.put("u1", new HashMap<>(Map.of("x", 30, "y", 5)));
        st.holdings.put("u2", new HashMap<>(Map.of("x", 10)));
        st.cur("x").vol = 4000;
        st.cur("y").vol = 0;
        for (int w = 0; w < 5; w++) {
            EconExchange.week(st, List.of("x", "y"), String::toUpperCase);
            o = rec.get(w + 2).getAsJsonObject();
            assertEquals(o.get("fairX").getAsDouble(), st.shares.get("x").fair, E, "fairX " + w);
            assertEquals(o.get("fairY").getAsDouble(), st.shares.get("y").fair, E, "fairY " + w);
            assertEquals(o.get("u1").getAsDouble(), st.div.getOrDefault("u1", 0.0), E, "u1 " + w);
            assertEquals(o.get("u2").getAsDouble(), st.div.getOrDefault("u2", 0.0), E, "u2 " + w);
            assertEquals(o.get("volX").getAsDouble(), st.cur("x").vol, E);
            st.cur("x").vol = 4000 - w * 900;
        }
    }
}
