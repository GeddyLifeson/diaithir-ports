package net.diaithir.worldsys.economy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Every data file loads, and a whole simulated year of the real economy runs without error. */
class EconDataTest {
    @Test
    void loadsAndRuns() throws Exception {
        EconData d = EconData.load();
        assertEquals(51011, d.prices.size());
        assertEquals(18, d.designs.size());
        assertEquals(56, d.vendors.size());
        assertEquals(512, d.drinks.size());
        assertNotNull(d.good("minecraft:iron_ingot"));
        // a run through the real tables: two cultures, a bank, a company, a market, a year of weeks
        EconData.DesignData salund = d.designs.get("salund");
        EconState st = new EconState();
        var regions = java.util.Map.of("a", new double[] {0, 0}, "b", new double[] {3000, 0});
        List<String> ids = new ArrayList<>(List.of("minecraft:wheat", "minecraft:iron_ingot", "minecraft:beef", "minecraft:leather"));
        for (String id : ids) { st.market.held("a").put(id, d.good(id).usual() * 1.8); st.market.held("b").put(id, d.good(id).usual() * 0.4); }
        EconPolity.mint(st, "x", 5000);
        st.cur("x").ref = 5000;
        EconState.Company co = EconCompany.found(st, "k", "Acme", "x");
        co.money = 3000;
        EconState.Line line = new EconState.Line();
        line.from = "a"; line.to = "b"; line.vehicle = "ox_cart"; line.n = 2;
        co.lines.add(line);
        for (int week = 0; week < 52; week++) {
            st.week = week;
            EconPolity.week(st, "x", "salund", EconPolity.Kind.COMMODITY, 40, EconPolity.rate(st, "x", salund.design(), "salund", EconPolity.Kind.COMMODITY, id -> {
                var g = d.good(id); return g == null ? 1.0 : EconModel.mid(st.market, regions, d::good, "a", id, null, 0); }));
            EconCompany.week(st, regions, d::good, c -> 1.0, c -> ids);
            EconFrictions.spoil(st, d::good, null, null);
            EconFrictions.season(st, d::good, week, null);
            EconBank.week(st.bank("x"), "V", 1.0);
            EconBonds.week(st, "x");
        }
        assertTrue(Double.isFinite(co.money));
        // the state survives a save and a load: the same money, lines, banks, bonds and stock
        EconState back = EconJson.fromJson(EconJson.toJson(st));
        assertEquals(EconJson.toJson(st), EconJson.toJson(back));
        assertEquals(co.money, back.company.get("k").money);
        assertEquals(st.market.stock.get("a").get("minecraft:wheat"), back.market.stock.get("a").get("minecraft:wheat"));
        assertTrue(line.units > 0, "the line hauled goods: " + line.units);
        for (var region : st.market.stock.values()) for (double v : region.values()) assertTrue(Double.isFinite(v) && v >= 0);
    }

    @Test
    void gatesComeFromTheGraph() throws Exception {
        EconData d = EconData.load();
        assertEquals(45, d.gates.size());
        assertEquals("VI", d.ageOfGood("diaithircrafting:foundry_drill"));
        assertEquals("RODOS", d.ageOfGood("diaithircrafting:foundry_research_pack_7"));
        assertEquals("I", d.ageOfGood("diaithircombat:lib_sword")); // gear a1..a9 share one item; it opens with the first
        assertEquals("III", d.ageOfGood("minecraft:iron_ingot")); // hand-priced staples keep their own Age
    }

    @Test
    void craftedItemsArePricedFromTheirRecipes() throws Exception {
        EconData d = EconData.load();
        // pack_4: copper ingot 12 + redstone 8 + gold nugget 6, each plus 0.25 tw labour
        assertEquals(26.75, d.good("diaithircrafting:foundry_research_pack_4").base(), 1e-9);
        assertEquals("VI", d.ageOfGood("diaithircrafting:foundry_research_pack_4"));
    }
}
