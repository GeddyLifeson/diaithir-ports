package net.diaithir.worldsys.economy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.function.Function;
import org.junit.jupiter.api.Test;

/** The polity and bank scenarios of tools/economy/golden_bank.js, replayed in Java against the numbers the KubeJS code gave. */
class EconBankTest {
    private static final double E = 1e-9;

    private static JsonObject golden() {
        return JsonParser.parseReader(new InputStreamReader(EconBankTest.class.getResourceAsStream("/econ_bank_golden.json"))).getAsJsonObject();
    }

    private static void cur(JsonObject w, EconState st, String c) {
        EconState.Cur s = st.cur(c);
        assertEquals(w.get("money").getAsDouble(), s.money, E, "money");
        assertEquals(w.get("treasury").getAsDouble(), s.treasury, E, "treasury");
        assertEquals(w.get("wagePool").getAsDouble(), s.wagePool, E, "wagePool");
        assertEquals(w.get("arrears").getAsDouble(), s.arrears, E, "arrears");
        assertEquals(w.get("tb").getAsDouble(), s.tb, E, "tb");
        assertEquals(w.get("taxMul").getAsDouble(), s.taxMul, E, "taxMul");
        assertEquals(w.get("infl").getAsDouble(), s.infl, E, "infl");
        assertEquals(w.get("weak").getAsDouble(), s.weak, E, "weak");
        assertEquals(w.get("reform").getAsBoolean(), s.reform, "reform");
    }

    private static void bank(JsonObject w, EconState.Bank b) {
        assertEquals(w.get("equity").getAsDouble(), b.equity, E, "equity");
        assertEquals(w.get("fund").getAsDouble(), b.fund, E, "fund");
        assertEquals(w.get("failed").getAsBoolean(), b.failed, "failed");
        assertEquals(w.get("dep").getAsDouble(), EconBank.deposits(b), E, "dep");
        assertEquals(w.get("loans").getAsDouble(), EconBank.loans(b), E, "loans");
        assertEquals(w.get("goods").getAsDouble(), b.goods.values().stream().mapToDouble(l -> l.owed).sum(), E, "goods");
    }

    @Test
    void polity() {
        var g = golden().getAsJsonArray("polity");
        EconState st = new EconState();
        EconPolity.Design design = new EconPolity.Design("salt", 1, "m", 2);
        Function<String, Double> mid = id -> id.equals("m") ? 3.0 : 1.0;
        EconPolity.mint(st, "x", 1000);
        st.cur("x").ref = 2000;
        JsonObject r0 = g.get(0).getAsJsonObject();
        assertEquals(r0.get("coin").getAsDouble(), EconPolity.rate(st, "x", design, "d", EconPolity.Kind.COIN, mid), E);
        assertEquals(r0.get("fiat").getAsDouble(), EconPolity.rate(st, "x", design, "d", EconPolity.Kind.FIAT, mid), E);
        assertEquals(r0.get("com").getAsDouble(), EconPolity.rate(st, "x", design, "d", EconPolity.Kind.COMMODITY, mid), E);
        EconPolity.tax(st, "x", 0.1, 200);
        cur(g.get(1).getAsJsonObject(), st, "x");
        EconPolity.week(st, "x", "d", EconPolity.Kind.COIN, 500, 5);
        cur(g.get(2).getAsJsonObject(), st, "x");
        assertEquals(g.get(3).getAsJsonObject().get("debase").getAsDouble(), st.des("d").debase, E);
        assertEquals(g.get(4).getAsJsonObject().get("wage").getAsDouble(), EconPolity.payWage(st, "x", 10), E);
        st.cur("x").wagePool = 50;
        assertEquals(g.get(5).getAsJsonObject().get("wage").getAsDouble(), EconPolity.payWage(st, "x", 10), E);
        cur(g.get(6).getAsJsonObject(), st, "x");
        for (int i = 0; i < 6; i++) EconPolity.week(st, "y", "d2", EconPolity.Kind.COMMODITY, 100, 1.0 / (1 + i));
        cur(g.get(7).getAsJsonObject(), st, "y");
        EconState.Peg peg = new EconState.Peg(); peg.perUnit = 1; peg.good = "salt";
        st.cur("z").peg = peg; EconPolity.mint(st, "z", 100); st.cur("z").ref = 100;
        assertEquals(g.get(8).getAsJsonObject().get("red").getAsBoolean(), EconPolity.redeemable(st, "z", "d"));
    }

    @Test
    void fractionalReserveBank() {
        var g = golden().getAsJsonArray("bank");
        EconState st = new EconState();
        EconState.Bank b = st.bank("x");
        EconBank.deposit(b, "p", 1000);
        int i = 0;
        assertEquals(g.get(i++).getAsJsonObject().get("cap").getAsDouble(), EconBank.capacity(b, "V"), E);
        assertEquals(g.get(i++).getAsJsonObject().get("r").getAsBoolean(), EconBank.borrow(b, "p", 800, "V").ok());
        bank(g.get(i++).getAsJsonObject(), b);
        EconBank.deposit(b, "q", 400);
        JsonObject q = g.get(i++).getAsJsonObject();
        assertEquals(q.get("r").getAsBoolean(), EconBank.borrow(b, "q", 100, "V").ok());
        assertEquals(q.get("w").getAsDouble(), EconBank.withdraw(b, "q", 400), E);
        EconBank.borrow(b, "q", 100, "V");
        for (int w = 0; w < 20; w++) {
            assertEquals(g.get(i++).getAsJsonObject().get("notes").getAsInt(), EconBank.week(b, "V", 1 + w * 0.01).size(), "notes at week " + w);
            if (w % 5 == 0) bank(g.get(i++).getAsJsonObject(), b);
        }
        assertEquals(g.get(i++).getAsJsonObject().get("rep").getAsDouble(), EconBank.repay(b, "p", 50), E);
        bank(g.get(i++).getAsJsonObject(), b);
        JsonObject rates = g.get(i++).getAsJsonObject().getAsJsonObject("rates");
        EconBank.Rates r = EconBank.rates(b, "V");
        assertEquals(rates.get("policy").getAsDouble(), r.policy(), E);
        assertEquals(rates.get("lend").getAsDouble(), r.lend(), E);
        assertEquals(rates.get("save").getAsDouble(), r.save(), E);
    }

    @Test
    void templeVault() {
        var g = golden().getAsJsonArray("vault");
        EconState st = new EconState();
        EconState.Bank b = st.bank("v");
        EconBank.vaultEndow(st, "v", b); EconBank.vaultEndow(st, "v", b);
        bank(g.get(0).getAsJsonObject(), b);
        assertEquals(g.get(1).getAsJsonObject().get("m").getAsDouble(), st.cur("v").money, E);
        EconState.Pledge p1 = pledge(160, 2);
        JsonObject o = g.get(2).getAsJsonObject();
        assertEquals(o.get("b1").getAsBoolean(), EconBank.vaultBorrow(b, "a", 100, p1, "III").ok());
        assertEquals(o.get("b2").getAsBoolean(), EconBank.vaultBorrow(b, "a", 10, pledge(160, 0), "III").ok());
        assertEquals(o.get("b3").getAsBoolean(), EconBank.vaultBorrow(b, "c", 100, pledge(100, 0), "III").ok());
        assertEquals(o.get("b4").getAsBoolean(), EconBank.vaultBorrow(b, "c", 100, pledge(200, 0), "III").ok());
        bank(g.get(3).getAsJsonObject(), b);
        for (int i = 0; i < 10; i++) EconBank.week(b, "III", 1);
        bank(g.get(4).getAsJsonObject(), b);
        assertEquals(g.get(5).getAsJsonObject().get("rep").getAsDouble(), EconBank.vaultRepay(b, "a", 30).taken(), E);
    }

    private static EconState.Pledge pledge(double value, int items) {
        EconState.Pledge p = new EconState.Pledge();
        p.value = value;
        for (int i = 0; i < items; i++) p.items.add(new EconState.Item());
        return p;
    }
}
