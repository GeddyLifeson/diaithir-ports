package net.diaithir.worldsys.economy;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

/** Bonds, companies, the founding office and the caravan job, replayed against tools/economy/golden_company.js. */
class EconCompanyTest {
    private static final double E = 1e-9;

    private static JsonObject golden() {
        return JsonParser.parseReader(new InputStreamReader(EconCompanyTest.class.getResourceAsStream("/econ_company_golden.json"))).getAsJsonObject();
    }

    @Test
    void bonds() {
        JsonArray g = golden().getAsJsonArray("bonds");
        EconState st = new EconState();
        EconPolity.mint(st, "x", 5000);
        st.cur("x").ref = 5000;
        EconState.Bond b = EconBonds.issue(st, "x", "p", 1000, 0.03);
        int i = 0;
        JsonObject o = g.get(i++).getAsJsonObject();
        assertEquals(o.get("rate").getAsDouble(), b.rate, E);
        assertEquals(o.get("treasury").getAsDouble(), st.cur("x").treasury, E);
        assertEquals(o.get("money").getAsDouble(), st.cur("x").money, E);
        for (int w = 0; w < 30; w++) {
            int notes = EconBonds.week(st, "x").size();
            if (w % 5 == 0 || notes > 0) {
                o = g.get(i++).getAsJsonObject();
                assertEquals(o.get("i").getAsInt(), w);
                assertEquals(o.get("notes").getAsInt(), notes);
                assertEquals(o.get("treasury").getAsDouble(), st.cur("x").treasury, E, "treasury " + w);
                assertEquals(o.get("due").getAsDouble(), st.bonds("x").due.getOrDefault("p", 0.0), E, "due " + w);
                assertEquals(o.get("arrears").getAsDouble(), st.cur("x").arrears, E);
                assertEquals(o.get("left").getAsInt(), st.bonds("x").list.isEmpty() ? 0 : st.bonds("x").list.get(0).left);
            }
        }
        o = g.get(i++).getAsJsonObject();
        assertEquals(o.get("got").getAsDouble(), EconBonds.collect(st, "x", "p"), E);
        assertEquals(o.get("money").getAsDouble(), st.cur("x").money, E);
        st.cur("y").arrears = 400; st.cur("y").ref = 2000;
        assertEquals(g.get(i++).getAsJsonObject().get("rate").getAsDouble(), EconBonds.rate(st, "y", 0.04), E);
        st.cur("y").treasury = 0;
        EconBonds.issue(st, "y", "q", 100, 0.04);
        st.cur("y").treasury = 0;
        o = g.get(i++).getAsJsonObject();
        JsonArray notes = o.getAsJsonArray("notes");
        for (int w = 0; w < 12; w++) assertEquals(notes.get(w).getAsInt(), EconBonds.week(st, "y").size(), "y week " + w);
        assertEquals(o.get("blot").getAsInt(), st.bonds("y").blot);
    }

    @Test
    void company() {
        JsonArray g = golden().getAsJsonArray("company");
        Map<String, EconModel.Good> goods = Map.of("minecraft:wheat", new EconModel.Good(1, 500), "minecraft:iron_ingot", new EconModel.Good(4, 200), "minecraft:flint", new EconModel.Good(0.5, 400));
        List<String> ids = new ArrayList<>(List.of("minecraft:wheat", "minecraft:iron_ingot", "minecraft:flint"));
        Map<String, double[]> regs = Map.of("a", new double[] {0, 0}, "b", new double[] {4000, 0});
        EconState st = new EconState();
        st.week = 3;
        st.market.held("a").putAll(Map.of("minecraft:wheat", 900.0, "minecraft:iron_ingot", 200.0, "minecraft:flint", 300.0));
        st.market.held("b").putAll(Map.of("minecraft:wheat", 100.0, "minecraft:iron_ingot", 120.0, "minecraft:flint", 400.0));
        EconState.Company co = EconCompany.found(st, "k", "Acme", "x");
        co.money = 5000;
        st.cur("x").treasury = 3000;
        co.lines.add(line("a", "b", "ox_cart", 2));
        co.lines.add(line("a", "b", "sailing_ship", 1));
        assertEquals(g.get(0).getAsJsonObject().get("assets").getAsDouble(), EconCompany.assets(co), E);
        for (int w = 0; w < 6; w++) {
            List<String> notes = EconCompany.week(st, regs, goods::get, c -> 2.0, c -> ids);
            JsonObject o = g.get(w + 1).getAsJsonObject();
            assertEquals(o.getAsJsonArray("notes").size(), notes.size(), "notes " + w);
            assertEquals(o.get("money").getAsDouble(), co.money, E, "money " + w);
            assertEquals(o.get("treasury").getAsDouble(), st.cur("x").treasury, E, "treasury " + w);
            assertEquals(o.get("broke").getAsInt(), co.broke);
            assertEquals(o.get("closed").getAsBoolean(), co.closed);
            for (int l = 0; l < 2; l++) {
                JsonObject lo = o.getAsJsonArray("lines").get(l).getAsJsonObject();
                EconState.Line line = co.lines.get(l);
                assertEquals(lo.get("rev").getAsDouble(), line.rev, E);
                assertEquals(lo.get("cost").getAsDouble(), line.cost, E);
                assertEquals(lo.get("upkeep").getAsDouble(), line.upkeep, E);
                assertEquals(lo.get("units").getAsInt(), line.units);
                assertEquals(lo.get("weeks").getAsInt(), line.weeks);
                assertEquals(lo.get("cargo").isJsonNull() ? null : lo.get("cargo").getAsString(), line.cargo);
            }
            assertEquals(o.get("wheatA").getAsDouble(), st.market.stock.get("a").get("minecraft:wheat"), E);
            assertEquals(o.get("wheatB").getAsDouble(), st.market.stock.get("b").get("minecraft:wheat"), E);
        }
        EconState.Company poor = EconCompany.found(st, "k2", "Poor", "x");
        poor.money = -1;
        poor.lines.add(line("a", "b", "pack_train", 1));
        for (int w = 0; w < 5; w++) EconCompany.week(st, regs, goods::get, c -> 2.0, c -> ids);
        JsonObject last = g.get(7).getAsJsonObject();
        assertEquals(last.get("poorClosed").getAsBoolean(), poor.closed);
        assertEquals(last.get("poorLines").getAsInt(), poor.lines.size());
        assertEquals(strings(last.getAsJsonArray("vehicles")), EconCompany.vehicles("III", t -> !t.equals("sail"), null).stream().map(EconCompany.Vehicle::id).toList());
        assertEquals(strings(last.getAsJsonArray("vehicles2")), EconCompany.vehicles("V", t -> true, 1850.0).stream().map(EconCompany.Vehicle::id).toList());
    }

    private static EconState.Line line(String from, String to, String vehicle, int n) {
        EconState.Line l = new EconState.Line();
        l.from = from; l.to = to; l.vehicle = vehicle; l.n = n;
        return l;
    }

    private static List<String> strings(JsonArray a) {
        List<String> out = new ArrayList<>();
        for (JsonElement e : a) out.add(e.getAsString());
        return out;
    }

    @Test
    void office() {
        JsonArray g = golden().getAsJsonArray("office");
        Map<String, String> w = new HashMap<>();
        EconCompany.World at = (x, y, z) -> {
            String id = w.getOrDefault(x + "," + y + "," + z, "minecraft:air");
            return new EconCompany.Block(id, id.endsWith(":air"));
        };
        for (int x = 0; x <= 5; x++) for (int z = 0; z <= 3; z++) for (int y = 0; y <= 3; y++)
            if (x == 0 || x == 5 || z == 0 || z == 3 || y == 0 || y == 3) w.put(x + "," + y + "," + z, "minecraft:stone");
        int[] s = {2, 1, 1};
        int i = 0;
        check(g.get(i++), EconCompany.officeCheck(at, s, "I", t -> true));
        w.put("0,1,2", "minecraft:oak_door"); w.put("1,1,1", "minecraft:lectern"); w.put("10,1,1", "minecraft:hay_block");
        check(g.get(i++), EconCompany.officeCheck(at, s, "I", t -> true));
        check(g.get(i++), EconCompany.officeCheck(at, s, "IV", t -> true));
        check(g.get(i++), EconCompany.officeCheck(at, s, "IV", t -> !t.equals("writing")));
        check(g.get(i++), EconCompany.officeCheck(at, s, "VI", t -> true));
        w.put("3,1,1", "minecraft:chest"); w.put("4,1,2", "minecraft:bookshelf"); w.put("4,2,2", "minecraft:bookshelf");
        w.put("10,1,2", "minecraft:barrel"); w.put("11,1,1", "minecraft:barrel"); w.put("12,1,1", "minecraft:barrel");
        check(g.get(i++), EconCompany.officeCheck(at, s, "IV", t -> true));
        check(g.get(i++), EconCompany.officeCheck(at, s, "VI", t -> true));
        check(g.get(i++), EconCompany.officeCheck(at, s, "VI", t -> !t.equals("railway")));
        w.remove("2,3,1");
        check(g.get(i++), EconCompany.officeCheck(at, s, "I", t -> true));
    }

    private static void check(JsonElement want, EconCompany.Office got) {
        JsonObject o = want.getAsJsonObject();
        assertEquals(o.get("ok").getAsBoolean(), got.ok());
        assertEquals(strings(o.getAsJsonArray("missing")), got.missing());
        if (!o.get("fee").isJsonNull()) assertEquals(o.get("fee").getAsInt(), got.fee());
    }

    @Test
    void caravan() {
        JsonObject g = golden().getAsJsonObject("caravan");
        String[] profs = {"toolsmith", "armorer", "weaponsmith", "mason", "farmer", "shepherd", "butcher", "fisherman", "leatherworker", "fletcher", "librarian", "cartographer", "cleric", "priest", "nitwit", "none", "stranger"};
        JsonArray jobs = g.getAsJsonArray("jobs");
        for (int p = 0; p < profs.length; p++) {
            JsonArray pair = jobs.get(p).getAsJsonArray();
            EconCompany.Job early = EconCompany.job(profs[p], "I"), late = EconCompany.job(profs[p], "V");
            assertEquals(pair.get(0).getAsJsonObject().get("id").getAsString(), early.id(), profs[p]);
            assertEquals(pair.get(0).getAsJsonObject().get("line").getAsString(), early.line(), profs[p]);
            assertEquals(pair.get(1).getAsJsonObject().get("id").getAsString(), late.id(), profs[p]);
        }
        List<EconCompany.Claim> claims = List.of(new EconCompany.Claim("a", 0, 0, "x"), new EconCompany.Claim("b", 50, 0, "x"), new EconCompany.Claim("c", 500, 0, "x"), new EconCompany.Claim("d", 300, 0, "y"));
        String[] from = {"a", "b", "c", "d", "zz"};
        for (int i = 0; i < from.length; i++) {
            JsonElement want = g.getAsJsonArray("dest").get(i);
            EconCompany.Claim got = EconCompany.dest(claims, from[i]);
            assertEquals(want.isJsonNull() ? null : want.getAsString(), got == null ? null : got.town());
        }
        double[][] r = {{2, 12, 1}, {2, 12, 0}, {0.1, 1, 0}, {3.7, 12, 1}};
        for (int i = 0; i < r.length; i++) assertEquals(g.getAsJsonArray("rewards").get(i).getAsLong(), EconCompany.reward(r[i][0], (int) r[i][1], r[i][2] == 1));
    }
}
