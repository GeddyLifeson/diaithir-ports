package net.diaithir.worldsys.economy;

import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;

/** Spoilage, coin wear, velocity and seasons. Port of 04_frictions.js. Only stock above the usual level spoils, so a shortage is never made worse by it. */
public final class EconFrictions {
    private EconFrictions() {}

    public static final double HOLD_LOSS = 0.003, WEAR = 0.0005, SEASON_SWING = 0.2, HARVEST = 0.75;

    private static final Object[][] SPOIL = {
        {Pattern.compile("^(honey_bottle|honeycomb|honeycomb_block|leather|salt_.*|sugar|rotten_flesh|spider_eye|rabbit_foot|rabbit_hide)$"), 0.0},
        {Pattern.compile("^(salted_|smoked_|pickled_|dried_(meat|fish|fruit)$)"), 0.02},
        {Pattern.compile("^(dried_kelp|hay_block)$"), 0.01},
        {Pattern.compile("^(raw_hide)$"), 0.25},
        {Pattern.compile("^(cooked_|bread|baked_potato|pumpkin_pie|cake|cookie|mushroom_stew|rabbit_stew|beetroot_soup)"), 0.15},
        {Pattern.compile("^(beef|porkchop|mutton|chicken|rabbit|cod|salmon|tropical_fish|pufferfish)$"), 0.2},
        {Pattern.compile("^(apple|carrot|potato|beetroot|melon_slice|sweet_berries|glow_berries|kelp|sea_pickle|pumpkin|melon)$"), 0.1},
        {Pattern.compile("^(wheat|wheat_seeds|beetroot_seeds|melon_seeds|pumpkin_seeds)$"), 0.03},
        {Pattern.compile("^egg$"), 0.06},
        {Pattern.compile("^milk_bucket$"), 0.3}};
    private static final Pattern SEASONAL = Pattern.compile("^(wheat|potato|carrot|beetroot|apple|melon_slice|melon|pumpkin|sweet_berries|glow_berries|sugar_cane|bread|hay_block)$");

    /** A drink's shelf life in days (half the batch gone by then) as a weekly rate. */
    public static double drinkRate(double days) { return days > 0 ? 1 - Math.pow(0.5, 7 / days) : 0; }

    /** The RimWorld curve: nothing spoils at or below 0 C, the full rate from 10 C up; null is room temperature. */
    public static double warmth(Double t) { return t == null || t.isNaN() ? 1 : t <= 0 ? 0 : t >= 10 ? 1 : t / 10; }

    /** Weekly spoilage rate of an item; the spoilage pack's own table may override the built-in one (drinkDays maps a drink to its shelf life). */
    public static double spoilRate(String id, Function<String, Double> drinkDays) {
        String b = id.substring(id.indexOf(':') + 1);
        if (id.startsWith("diaithir_drinks:")) return drinkDays == null ? 0 : drinkRate(drinkDays.apply(b) == null ? 0 : drinkDays.apply(b));
        for (Object[] s : SPOIL) if (((Pattern) s[0]).matcher(b).find()) return (Double) s[1];
        return 0;
    }

    /** Loses stock above usual in every region; returns the units lost. warmth gives a region's share of the room-temperature rate. */
    public static double spoil(EconState st, Function<String, EconModel.Good> good, Function<String, Double> warmth, Function<String, Double> drinkDays) {
        double lost = 0;
        for (Map.Entry<String, Map<String, Double>> region : st.market.stock.entrySet()) {
            double w = warmth == null ? 1 : warmth.apply(region.getKey());
            for (Map.Entry<String, Double> e : region.getValue().entrySet()) {
                EconModel.Good g = good.apply(e.getKey());
                if (g == null || e.getValue() <= g.usual()) continue;
                double loss = (e.getValue() - g.usual()) * Math.min(1, spoilRate(e.getKey(), drinkDays) * w + HOLD_LOSS);
                e.setValue(e.getValue() - loss);
                lost += loss;
            }
        }
        return lost;
    }

    /** Coin wears; the crown recoins when its treasury can pay for the metal lost. Returns the units paid for recoinage. */
    public static double wear(EconState st, String culture, String did) {
        EconState.Cur s = st.cur(culture);
        EconState.Des d = st.des(did);
        double w = (1 - d.debase) * WEAR;
        d.debase += w;
        d.worn += w;
        double cost = d.worn * Math.max(1, s.money);
        if (s.treasury < cost) return 0;
        s.treasury -= cost;
        d.debase = Math.max(0, d.debase - d.worn);
        d.worn = 0;
        return cost;
    }

    public static double velocity(EconState.Cur s) { return s.money > 0 ? s.vol / s.money : 0; }

    /** Seasons: farm goods follow a cycle around their usual level; only the change since the last tick is added. phaseOf gives the crops' year fraction (null: 52-week clock). */
    public static void season(EconState st, Function<String, EconModel.Good> good, long week, Function<String, Double> phaseOf) {
        for (Map.Entry<String, Map<String, Double>> region : st.market.stock.entrySet()) {
            Double y = phaseOf == null ? null : phaseOf.apply(region.getKey());
            if (y == null) y = (week / 52.0) % 1;
            double cyc = SEASON_SWING * Math.cos(2 * Math.PI * (y - HARVEST));
            Double prev = st.season.put(region.getKey(), cyc);
            if (prev == null) continue;
            for (Map.Entry<String, Double> e : region.getValue().entrySet()) {
                EconModel.Good g = good.apply(e.getKey());
                if (g != null && SEASONAL.matcher(e.getKey().substring(e.getKey().indexOf(':') + 1)).find())
                    e.setValue(Math.max(1, e.getValue() + g.usual() * (cyc - prev)));
            }
        }
    }
}
