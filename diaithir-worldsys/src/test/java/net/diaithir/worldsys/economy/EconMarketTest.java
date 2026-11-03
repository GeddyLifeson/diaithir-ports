package net.diaithir.worldsys.economy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Which money each culture uses in each Age, replayed against tools/economy/golden_ctx.js; and the context a market builds from it. */
class EconMarketTest {
    @Test
    void designChoiceMatches() throws Exception {
        EconData d = EconData.load();
        JsonObject g = JsonParser.parseReader(new InputStreamReader(EconMarketTest.class.getResourceAsStream("/econ_ctx_golden.json"))).getAsJsonObject();
        List<Map<String, String>> own = List.of(Map.of(), d.cultureMoney);
        for (JsonElement e : g.getAsJsonArray("rows")) {
            var r = e.getAsJsonArray();
            String culture = r.get(0).getAsString(), age = r.get(1).getAsString();
            EconPolity.Kind kind = EconPolity.kindAt(age);
            assertEquals(r.get(2).getAsString(), kind.name().toLowerCase());
            assertEquals(r.get(3).isJsonNull() ? null : r.get(3).getAsString(), EconMarket.designOf(d.designs, culture, kind, own, age), culture + " " + age);
        }
        assertEquals(g.getAsJsonArray("reform").get(0).getAsString(), EconMarket.designOf(d.designs, "adena", EconPolity.Kind.COIN, List.of(Map.of("adena", "uma"), d.cultureMoney), "V"));
    }

    @Test
    void contextBuilds() throws Exception {
        EconData d = EconData.load();
        EconState st = new EconState();
        var regions = Map.of("t", new double[] {0, 0});
        for (String age : List.of("I", "III", "V", "VI")) {
            EconMarket.Ctx c = EconMarket.ctx(st, d, "adena", age, List.of(Map.of(), d.cultureMoney),
                id -> d.good(id) == null ? 1.0 : EconModel.mid(st.market, regions, d::good, "t", id, null, 0), String::toUpperCase);
            assertNotNull(c.design());
            assertTrue(c.rate() > 0 && Math.abs(c.scale() * c.rate() - 1) < 1e-9);
            assertTrue(!c.denoms().isEmpty());
            assertTrue(c.unit().endsWith("ADENA"));
            assertTrue(st.cur("adena").money > 0, "the culture starts with money in circulation");
        }
        EconMarket.Ctx wilds = EconMarket.ctx(st, d, null, "V", List.of(), id -> 1.0, String::toUpperCase);
        assertEquals("wilds", wilds.culture());
        assertEquals(EconPolity.Kind.COMMODITY, wilds.kind());
    }
}
