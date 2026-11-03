package net.diaithir.worldsys.economy;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

/** Currencies, rates, minting, tax and the weekly treasury. Port of 01_polity.js; every real price is in tw (a quarter hour of unskilled work). */
public final class EconPolity {
    private EconPolity() {}

    public static final List<String> AGES = List.of("TELLING", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "RODOS");
    public static final double WAGE_HOUR = 4;
    public static final Map<String, Double> SKILL = Map.of("unskilled", 1.0, "trained", 1.6, "skilled", 2.5, "master", 4.0);
    public static final Map<String, Double> TAX_BY_AGE = Map.of("TELLING", 0.04, "I", 0.04, "II", 0.06, "III", 0.08, "IV", 0.1, "V", 0.1,
        "VI", 0.15, "VII", 0.18, "VIII", 0.18, "RODOS", 0.18);
    public static final double CHANGER_FEE = 0.03;
    public static final double TB_REF = 3000;

    public enum Kind { COMMODITY, COIN, FIAT }

    /** A currency design: a staple (commodity) and/or a coin of metal. */
    public record Design(String commodityGood, double perUnit, String coinMetal, double coinContent) {}

    static double clamp(double v, double lo, double hi) { return Math.min(hi, Math.max(lo, v)); }

    public static Kind kindAt(String age) {
        int i = AGES.indexOf(age);
        return i >= AGES.indexOf("VI") ? Kind.FIAT : i >= AGES.indexOf("V") ? Kind.COIN : Kind.COMMODITY;
    }

    /** Value in tw of one unit of a design before any culture's own factors. mid gives the real price of a good in tw. */
    public static double designValue(EconState st, Design design, String did, Kind kind, Function<String, Double> mid) {
        if (kind == Kind.COMMODITY) return design.perUnit() * mid.apply(design.commodityGood());
        EconState.Des d = st.des(did);
        double coin = design.coinContent() * mid.apply(design.coinMetal()) * (1 - d.debase);
        if (kind == Kind.COIN) return coin;
        if (d.rate0 == 0) d.rate0 = coin;
        return d.rate0;
    }

    /** Tw per unit of the culture's money. */
    public static double rate(EconState st, String culture, Design design, String did, Kind kind, Function<String, Double> mid) {
        EconState.Cur s = st.cur(culture);
        if (design == null) return 1;
        double base = designValue(st, design, did, kind, mid);
        if (kind == Kind.COIN && s.peg != null) base = s.peg.perUnit * mid.apply(s.peg.good) * (1 - st.des(did).debase);
        if (kind != Kind.COMMODITY) base *= clamp(Math.pow(Math.max(1, s.ref) / Math.max(1, s.money), kind == Kind.FIAT ? 0.5 : 0.4), 0.4, 2.5);
        return Math.max(1e-6, base * Math.exp(0.15 * clamp(s.tb, -1, 1)));
    }

    public static double convert(Function<String, Double> rate, String from, String to, double amount) {
        return from.equals(to) ? amount : amount * rate.apply(from) / rate.apply(to) * (1 - CHANGER_FEE);
    }

    /** Money enters or leaves circulation; a negative amount burns, and circulation never falls below zero. */
    public static double mint(EconState st, String culture, double amount) {
        EconState.Cur s = st.cur(culture);
        s.money = Math.max(0, s.money + amount);
        return s.money;
    }

    public static double taxRate(Map<String, Double> taxes, String culture, String age, EconState st) {
        if (taxes != null && taxes.containsKey(culture)) return taxes.get(culture);
        double mul = st != null && st.cur.containsKey(culture) ? st.cur.get(culture).taxMul : 1;
        return TAX_BY_AGE.getOrDefault(age, 0.1) * mul;
    }

    /** Tax on a trade worth value units leaves circulation and lands in the treasury. */
    public static double tax(EconState st, String culture, double rate, double value) {
        double t = value * rate;
        mint(st, culture, -t);
        st.cur(culture).treasury += t;
        return t;
    }

    public static void flow(EconState st, String culture, double twValue) { st.cur(culture).flow += twValue; }

    public static double wage(Function<String, Double> rate, String culture, double hours, String skill) {
        return hours * WAGE_HOUR * SKILL.getOrDefault(skill == null ? "unskilled" : skill, 1.0) / rate.apply(culture);
    }

    /** A job draws from the wage pool; a dry pool leaves arrears and pays 0. */
    public static double payWage(EconState st, String culture, double amount) {
        EconState.Cur s = st.cur(culture);
        if (s.wagePool < amount) { s.arrears += amount; return 0; }
        s.wagePool -= amount;
        mint(st, culture, amount);
        return amount;
    }

    /** Once a week: the treasury pays the wage bill, a shortfall is met from the mint (commodity money goes into arrears), the rest feeds the wage pool. */
    public static EconState.Cur week(EconState st, String culture, String did, Kind kind, double wageBill, double rate) {
        EconState.Cur s = st.cur(culture);
        if (rate != 0) watch(s, kind, rate);
        s.tb = clamp(s.tb * 0.8 + s.flow / TB_REF, -1, 1);
        s.flow = 0;
        double shortfall = Math.max(0, wageBill - s.treasury);
        s.treasury = Math.max(0, s.treasury - wageBill);
        if (shortfall > 0) {
            if (kind == Kind.COMMODITY) s.arrears += shortfall;
            else {
                double before = Math.max(1, s.money);
                mint(st, culture, shortfall);
                if (kind == Kind.COIN && did != null) {
                    EconState.Des d = st.des(did);
                    d.debase = Math.min(0.9, 1 - (1 - d.debase) * Math.pow(before / (before + shortfall), 0.5));
                }
            }
        }
        s.taxMul = clamp(s.taxMul * (shortfall > 0 ? 1.05 : s.treasury > 4 * wageBill && wageBill > 0 ? 0.97 : 1), 0.5, 2);
        double pool = Math.min(s.treasury * 0.3, wageBill);
        s.treasury -= pool;
        s.wagePool += pool;
        return s;
    }

    /** Four weeks below 55% of the starting rate and a commodity money reforms. */
    public static void watch(EconState.Cur s, Kind kind, double rate) {
        if (s.rateRef == 0) s.rateRef = rate;
        s.infl = s.prevRate != 0 ? s.prevRate / rate - 1 : 0;
        s.prevRate = rate;
        s.weak = kind == Kind.COMMODITY && rate < 0.55 * s.rateRef ? s.weak + 1 : 0;
        if (s.weak >= 4) s.reform = true;
    }

    public static boolean redeemable(EconState st, String culture, String did) {
        EconState.Cur s = st.cur(culture);
        if (s.peg == null) return false;
        return Math.pow(Math.max(1, s.ref) / Math.max(1, s.money), 0.4) * (1 - st.des(did).debase) >= 0.9;
    }
}
