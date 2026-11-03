package net.diaithir.worldsys.economy;

import java.util.List;
import java.util.Map;

/** The exchange: one joint-stock company per culture's merchants from Age V. Port of exchange.js (the maths; the commands live on the NeoForge side). */
public final class EconExchange {
    private EconExchange() {}

    public static final String AGE = "V";
    public static final double PAR = 100, FLOAT = 1000, FEE = 0.005, PAYOUT = 0.6, YIELD = 0.06, CUT = 0.02;

    public static EconState.ShareCompany charter(EconState st, String culture, String name) {
        return st.shares.computeIfAbsent(culture, k -> {
            EconState.ShareCompany c = new EconState.ShareCompany();
            c.name = name; c.culture = culture; c.float_ = FLOAT; c.fair = PAR;
            return c;
        });
    }

    public static double price(EconState.ShareCompany c) { return Math.max(0.01, c.fair * Math.exp(c.z / c.float_)); }

    /** Cost in tw of moving n shares from z (n > 0 buying, n < 0 selling): the area under the curve. */
    public static double cost(EconState.ShareCompany c, double n) { return c.fair * c.float_ * (Math.exp((c.z + n) / c.float_) - Math.exp(c.z / c.float_)); }

    /** Units paid for n shares at the market's scale; or received, less the broker's cut. Moves the price and the holder's shares. */
    public static long trade(EconState st, String holder, String co, int n, boolean buying, double scale) {
        EconState.ShareCompany c = st.shares.get(co);
        Map<String, Integer> held = st.holdings.computeIfAbsent(holder, k -> new java.util.HashMap<>());
        if (!buying) n = Math.min(n, held.getOrDefault(co, 0));
        if (n < 1) return 0;
        double tw = buying ? cost(c, n) : -cost(c, -n);
        long units = buying ? (long) Math.ceil(tw * (1 + FEE) * scale) : (long) Math.floor(tw * (1 - FEE) * scale);
        c.z += buying ? n : -n;
        held.merge(co, buying ? n : -n, Integer::sum);
        return units;
    }

    /** Once a week: each company takes its cut of the trade in its culture, accrues dividends, and its fair value moves toward its yield. */
    public static void week(EconState st, List<String> cultures, java.util.function.Function<String, String> nameOf) {
        for (String culture : cultures) charter(st, culture, "Kompunaid " + nameOf.apply(culture));
        for (Map.Entry<String, EconState.ShareCompany> e : st.shares.entrySet()) {
            EconState.ShareCompany c = e.getValue();
            EconState.Cur s = c.culture != null ? st.cur(c.culture) : null;
            double earn = s != null ? s.vol * CUT : 0;
            if (s != null) s.vol = 0;
            double perShare = earn * PAYOUT / c.float_;
            if (perShare > 0) for (Map.Entry<String, Map<String, Integer>> h : st.holdings.entrySet()) {
                Integer n = h.getValue().get(e.getKey());
                if (n != null && n != 0) st.div.merge(h.getKey(), n * perShare, Double::sum);
            }
            c.fair = Math.max(5, c.fair * 0.8 + Math.max(5, 52 * perShare / YIELD) * 0.2);
        }
    }
}
