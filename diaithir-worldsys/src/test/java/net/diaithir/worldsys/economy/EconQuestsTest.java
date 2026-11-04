package net.diaithir.worldsys.economy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** The Safe passage and Found a company chain, played through with a fake player. */
class EconQuestsTest {
    static final class Fake implements EconQuests.Player {
        final Map<String, Integer> inv = new HashMap<>();
        final List<String> said = new ArrayList<>(), ran = new ArrayList<>();
        public String id() { return "p1"; }
        public int count(String item) { return inv.getOrDefault(item, 0); }
        public void take(String item, int n) { inv.merge(item, -n, Integer::sum); }
        public void give(String item, int n) { inv.merge(item, n, Integer::sum); }
        public void tell(String text) { said.add(text); }
        public void command(String command) { ran.add(command); }
        public String itemName(String item) { return item.substring(item.indexOf(':') + 1); }
    }

    @Test
    void caravanThenCompany() throws Exception {
        EconData data = EconData.load();
        EconState st = new EconState();
        List<EconClaims.Claim> claims = List.of(new EconClaims.Claim(0, 0, 600, "adena", "adena", "a", "Alpha"), new EconClaims.Claim(500, 0, 600, "adena", "adena", "b", "Beta"));
        var regions = Map.of("a", new double[] {0, 0}, "b", new double[] {500, 0});
        EconQuests.World w = new EconQuests.World(st, data, claims, c -> EconMarket.ctx(st, data, c, "III", List.of(Map.of(), data.cultureMoney),
            id -> data.good(id) == null ? 1.0 : EconModel.mid(st.market, regions, data::good, "a", id, null, 0), String::toUpperCase), true);
        Fake p = new Fake();
        EconQuests.Place alpha = new EconQuests.Place("a", "Alpha", "adena", "III"), beta = new EconQuests.Place("b", "Beta", "adena", "III");
        for (int i = 0; i < 2; i++) EconQuests.talk(w, p, alpha, "farmer");
        assertNull(st.quest("p1").caravan, "two talks are not enough");
        EconQuests.talk(w, p, alpha, "farmer");
        assertEquals("active", st.quest("p1").caravan);
        assertEquals(12, p.count("minecraft:wheat"), "the load is handed over");
        assertEquals("b", st.quest("p1").job.to);
        assertTrue(p.ran.contains("ages quest report caravan accepted"));
        // not at the destination: nothing happens; at the destination without the load: told what is missing
        EconQuests.talk(w, p, alpha, "farmer");
        p.inv.put("minecraft:wheat", 5);
        EconQuests.talk(w, p, beta, "mason");
        assertTrue(p.said.get(p.said.size() - 1).contains("You carry 5"));
        p.inv.put("minecraft:wheat", 12);
        st.cur("adena").wagePool = 1000;
        double pool = st.cur("adena").wagePool;
        EconQuests.talk(w, p, beta, "mason");
        assertEquals("done", st.quest("p1").caravan);
        assertEquals("offered", st.quest("p1").company);
        assertEquals(0, p.count("minecraft:wheat"));
        assertTrue(st.cur("adena").wagePool < pool, "the fee came from the town's wage pool");
        EconMarket.Ctx ctx = w.ctxOf().apply("adena");
        assertTrue(EconPurse.own(p, ctx.denoms()) > 0, "and was paid in coins");
        assertTrue(p.ran.contains("ages quest report caravan delivered"));
        // a second delivery cannot start: the caravan is done
        int said = p.said.size();
        for (int i = 0; i < 5; i++) EconQuests.talk(w, p, alpha, "farmer");
        assertEquals(said, p.said.size());
    }

    @Test
    void lateAndDryTill() throws Exception {
        EconData data = EconData.load();
        EconState st = new EconState();
        List<EconClaims.Claim> claims = List.of(new EconClaims.Claim(0, 0, 600, "adena", "adena", "a", "Alpha"), new EconClaims.Claim(500, 0, 600, "adena", "adena", "b", "Beta"));
        EconQuests.World w = new EconQuests.World(st, data, claims, c -> EconMarket.ctx(st, data, c, "I", List.of(Map.of(), data.cultureMoney), id -> 1.0, String::toUpperCase), true);
        Fake p = new Fake();
        for (int i = 0; i < 3; i++) EconQuests.talk(w, p, new EconQuests.Place("a", "Alpha", "adena", "I"), "toolsmith");
        assertEquals("minecraft:flint", st.quest("p1").job.id, "Age I smiths carry flint");
        st.week = 20; // far past the deadline, and the wage pool is empty
        EconQuests.talk(w, p, new EconQuests.Place("b", "Beta", "adena", "I"), "mason");
        assertEquals("done", st.quest("p1").caravan);
        assertTrue(p.said.stream().anyMatch(s -> s.contains("late but whole")));
        assertTrue(p.said.stream().anyMatch(s -> s.contains("owes you")));
        assertEquals(0, EconPurse.own(p, EconMarket.ctx(st, data, "adena", "I", List.of(Map.of(), data.cultureMoney), id -> 1.0, String::toUpperCase).denoms()));
    }

    @Test
    void purse() {
        List<EconOffers.Denom> d = List.of(new EconOffers.Denom("big", 100), new EconOffers.Denom("small", 1));
        Fake p = new Fake();
        EconPurse.give(p, 345, d);
        assertEquals(3, p.count("big"));
        assertEquals(45, p.count("small"));
        assertEquals(345, EconPurse.own(p, d));
        assertTrue(EconPurse.take(p, 120, d));
        assertEquals(225, EconPurse.own(p, d));
        assertEquals(false, EconPurse.take(p, 999, d));
    }
}
