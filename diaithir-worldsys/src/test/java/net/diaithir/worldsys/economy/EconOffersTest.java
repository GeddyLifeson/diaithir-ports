package net.diaithir.worldsys.economy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Trade-screen planning, vendors and bottle values, replayed against tools/economy/golden_offers.js. */
class EconOffersTest {
    private static final double E = 1e-9;
    private static final List<EconOffers.Denom> DENOMS = List.of(new EconOffers.Denom("big", 100), new EconOffers.Denom("mid", 10), new EconOffers.Denom("small", 1));

    private static JsonObject read(String name) {
        return JsonParser.parseReader(new InputStreamReader(EconOffersTest.class.getResourceAsStream(name))).getAsJsonObject();
    }

    private static void splitIs(JsonElement want, EconOffers.Split got) {
        if (want.isJsonNull()) { assertNull(got); return; }
        JsonObject o = want.getAsJsonObject();
        assertEquals(o.get("units").getAsLong(), got.units());
        JsonArray cost = o.getAsJsonArray("cost");
        assertEquals(cost.size(), got.cost().size());
        for (int i = 0; i < cost.size(); i++) {
            assertEquals(cost.get(i).getAsJsonArray().get(0).getAsString(), got.cost().get(i).item());
            assertEquals(cost.get(i).getAsJsonArray().get(1).getAsDouble(), got.cost().get(i).count(), E);
        }
    }

    @Test
    void splitAndPayout() {
        JsonObject g = read("/econ_offers_golden.json");
        for (JsonElement e : g.getAsJsonArray("split")) splitIs(e.getAsJsonArray().get(1), EconOffers.split(e.getAsJsonArray().get(0).getAsDouble(), DENOMS));
        JsonArray one = g.getAsJsonArray("split1");
        List<EconOffers.Denom> coin = List.of(new EconOffers.Denom("coin", 1)), hl = List.of(new EconOffers.Denom("hi", 100), new EconOffers.Denom("lo", 50));
        splitIs(one.get(0).getAsJsonArray().get(1), EconOffers.split(3, coin));
        splitIs(one.get(1).getAsJsonArray().get(1), EconOffers.split(70, coin));
        splitIs(one.get(2).getAsJsonArray().get(1), EconOffers.split(64, coin));
        splitIs(one.get(3).getAsJsonArray().get(1), EconOffers.split(250, hl));
        for (JsonElement e : g.getAsJsonArray("payout")) {
            EconOffers.Payout p = EconOffers.payout(e.getAsJsonArray().get(0).getAsDouble(), DENOMS);
            JsonElement w = e.getAsJsonArray().get(1);
            if (w.isJsonNull()) { assertNull(p); continue; }
            assertEquals(w.getAsJsonArray().get(0).getAsString(), p.item());
            assertEquals(w.getAsJsonArray().get(1).getAsLong(), p.count());
            assertEquals(w.getAsJsonArray().get(2).getAsLong(), p.units());
        }
    }

    @Test
    void offerPlan() {
        JsonArray want = read("/econ_offers_golden.json").getAsJsonArray("plan");
        Map<String, Double> price = Map.of("a", 0.4, "b", 2.6, "c", 17.0, "d", 450.0, "e", 5.0);
        Map<String, Double> held = Map.of("a", 500.0, "b", 3.0, "c", 0.0, "d", 40.0);
        List<String> goods = List.of("x:a", "x:b", "x:c", "x:d", "x:e", "x:f", "x:g");
        List<EconOffers.Offer> got = EconOffers.offerPlan(goods, DENOMS,
            (id, n, buying) -> price.getOrDefault(id.substring(2), 1.0) * n * (buying ? 1.15 : 0.85), id -> held.getOrDefault(id.substring(2), 20.0), 4, 2);
        assertEquals(want.size(), got.size());
        for (int i = 0; i < want.size(); i++) {
            JsonObject o = want.get(i).getAsJsonObject();
            EconOffers.Offer f = got.get(i);
            assertEquals(o.get("dir").getAsString(), f.dir());
            assertEquals(o.get("id").getAsString(), f.id());
            assertEquals(o.get("n").getAsInt(), f.n());
            assertEquals(o.get("units").getAsLong(), f.units());
            if (f.dir().equals("buy")) {
                JsonArray cost = o.getAsJsonArray("cost");
                assertEquals(cost.size(), f.cost().size());
                for (int c = 0; c < cost.size(); c++) { assertEquals(cost.get(c).getAsJsonArray().get(0).getAsString(), f.cost().get(c).item()); assertEquals(cost.get(c).getAsJsonArray().get(1).getAsDouble(), f.cost().get(c).count(), E); }
            } else {
                assertEquals(o.getAsJsonArray("result").get(0).getAsString(), f.result().item());
                assertEquals(o.getAsJsonArray("result").get(1).getAsDouble(), f.result().count(), E);
            }
        }
    }

    @Test
    void vendorsAndStalls() {
        JsonObject g = read("/econ_offers_golden.json");
        List<String> open = List.of("minecraft:wheat", "minecraft:bread", "minecraft:iron_ingot", "minecraft:iron_sword", "minecraft:flint", "minecraft:cod", "minecraft:oak_log", "minecraft:stone",
            "minecraft:book", "minecraft:leather", "minecraft:stick", "minecraft:string", "minecraft:cobblestone", "minecraft:feather", "minecraft:paper", "x:salt_a", "x:honeycomb", "x:bone");
        List<EconOffers.Vendor> vendors = List.of(new EconOffers.Vendor("forager", List.of("minecraft:wheat", "minecraft:bread", "x:honeycomb")),
            new EconOffers.Vendor("flint_smith", List.of("minecraft:flint", "minecraft:stick", "minecraft:iron_ingot")), new EconOffers.Vendor("salt_trader", List.of("x:salt_a", "x:bone", "minecraft:oak_log")),
            new EconOffers.Vendor("smith", List.of("minecraft:iron_ingot", "minecraft:iron_sword")), new EconOffers.Vendor("baker", List.of("minecraft:bread")));
        String[] profs = {"farmer", "toolsmith", "mason", "nitwit", "priest", "librarian", "nobody", "armorer"};
        for (int i = 0; i < profs.length; i++) {
            EconOffers.Vendor v = EconOffers.vendorFor(profs[i], vendors);
            assertEquals(g.getAsJsonArray("vendorFor").get(i).getAsString(), v == null ? null : v.id(), profs[i]);
        }
        for (JsonElement e : g.getAsJsonArray("stall")) {
            JsonArray a = e.getAsJsonArray();
            String p = a.get(0).getAsString();
            double seed = a.get(1).getAsDouble();
            assertEquals(strings(a.get(2).getAsJsonArray()), EconOffers.stall(p, open, seed, vendors), p + " " + seed);
            assertEquals(strings(a.get(3).getAsJsonArray()), EconOffers.stall(p, open, seed, List.of()), p + " " + seed + " no vendors");
        }
    }

    private static List<String> strings(JsonArray a) {
        List<String> out = new ArrayList<>();
        for (JsonElement e : a) out.add(e.getAsString());
        return out;
    }

    @Test
    void drinks() {
        JsonObject g = read("/econ_offers_golden.json");
        Map<String, EconDrinks.Style> styles = new HashMap<>();
        read("/drink_value.json").entrySet().forEach(e -> {
            JsonObject o = e.getValue().getAsJsonObject();
            styles.put(e.getKey(), new EconDrinks.Style(o.has("abv") ? o.get("abv").getAsDouble() : 0, o.has("aged") && o.get("aged").getAsBoolean(), o.has("peak") ? o.get("peak").getAsDouble() : 0));
        });
        for (JsonElement e : g.getAsJsonArray("drinkFactor")) {
            JsonArray a = e.getAsJsonArray();
            assertEquals(a.get(3).getAsDouble(), EconDrinks.factor(styles, "diaithir_drinks:" + a.get(0).getAsString(), a.get(1).getAsDouble(), a.get(2).getAsDouble()), E, a.get(0).getAsString());
        }
        assertEquals(g.get("notDrink").getAsDouble(), EconDrinks.factor(styles, "minecraft:stone", 5, 5), E);
        for (JsonElement e : g.getAsJsonArray("bottle")) {
            JsonArray a = e.getAsJsonArray();
            EconDrinks.Bottle b = EconDrinks.bottleData(a.get(0).getAsString());
            assertEquals(a.get(1).getAsJsonObject().get("abv").getAsDouble(), b.abv(), E);
            assertEquals(a.get(1).getAsJsonObject().get("aged").getAsDouble(), b.aged(), E);
        }
    }
}
