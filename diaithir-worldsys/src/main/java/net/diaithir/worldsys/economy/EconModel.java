package net.diaithir.worldsys.economy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The price model, pure Java. Port of the KubeJS 00_model.js: prices move only through trades; demand has an elasticity, substitutes move
 * together, made goods are floored by their inputs, and towns trade away the gap beyond the freight. Money is in tw (see EconPolity).
 */
public final class EconModel {
    private EconModel() {}

    public static final double FREIGHT = 0.00002; // of base price per block per unit
    public static final double FLOOR_RAW = 0.3;
    public static final double FLOOR_MADE = 0.8;
    public static final double LABOUR = 0.25;
    public static final double SUBSTITUTION = 0.25;

    private static final Pattern LUXURY = Pattern.compile("diamond|emerald|ruby|sapphire|netherite|nether_star|amethyst|totem|golden_apple|music_disc|enchanted|elytra|spawn_egg|trident|beacon|echo|gold(_ingot|_block)?$|ender_|dragon|smithing_template|pottery_sherd");
    private static final Pattern NECESSITY = Pattern.compile("^(bread|wheat|beef|porkchop|mutton|chicken|cod|salmon|rabbit|potato|carrot|beetroot|apple|cooked_.*|salt_.*|oak_.*|spruce_.*|birch_.*|jungle_.*|acacia_.*|dark_oak_.*|cobblestone|stone|dirt|flint|leather|string|bone|feather|stick|coal|charcoal|torch|water_bucket|white_wool|clay_ball|sand|gravel)$");

    /** A good's [base price in tw, usual stock]. */
    public record Good(double base, double usual) {}

    /** Stock by town, then item id. */
    public static final class State {
        public final Map<String, Map<String, Double>> stock = new HashMap<>();
        public Map<String, Double> held(String region) { return stock.computeIfAbsent(region, k -> new HashMap<>()); }
    }

    static String path(String id) { return id.substring(id.indexOf(':') + 1); }

    public static double elasticity(String id) {
        String p = path(id);
        if (LUXURY.matcher(p).find()) return 1.8;
        if (NECESSITY.matcher(p).find()) return 0.4;
        return 1.0;
    }

    private static final Object[][] GROUPS = {
        {"metal", Pattern.compile("_ingot$")}, {"log", Pattern.compile("_log$|_stem$")},
        {"meat", Pattern.compile("^(cooked_)?(beef|porkchop|mutton|chicken|rabbit|cod|salmon)$")},
        {"grain", Pattern.compile("^(wheat|bread|baked_potato|potato|carrot|beetroot)$")},
        {"gem", Pattern.compile("^(diamond|emerald|amethyst_shard|lapis_lazuli|quartz)$|_gem$")}};

    public static String group(String id) {
        String p = path(id);
        for (Object[] g : GROUPS) if (((Pattern) g[1]).matcher(p).find()) return (String) g[0];
        return null;
    }

    private static final Map<String, String> MATERIAL = Map.of("iron", "minecraft:iron_ingot", "golden", "minecraft:gold_ingot",
        "diamond", "minecraft:diamond", "netherite", "minecraft:netherite_ingot", "copper", "minecraft:copper_ingot",
        "stone", "minecraft:cobblestone", "wooden", "minecraft:oak_planks", "chainmail", "minecraft:iron_ingot", "leather", "minecraft:leather");
    private static final Map<String, Integer> UNITS = Map.of("sword", 2, "axe", 3, "pickaxe", 3, "shovel", 1, "hoe", 2,
        "helmet", 5, "chestplate", 8, "leggings", 7, "boots", 4);
    private static final Pattern TOOL = Pattern.compile("^(\\w+?)_(sword|axe|pickaxe|shovel|hoe|helmet|chestplate|leggings|boots)$");
    private static final Pattern BLOCK = Pattern.compile("^(\\w+)_block$");

    /** [input item, units] for a made good, or null. */
    public static Object[] inputs(String id, Predicate<String> known) {
        String p = path(id);
        Matcher m = TOOL.matcher(p);
        if (m.find() && MATERIAL.containsKey(m.group(1))) return new Object[] {MATERIAL.get(m.group(1)), UNITS.get(m.group(2))};
        Matcher b = BLOCK.matcher(p);
        if (b.find()) {
            String ns = id.substring(0, id.indexOf(':'));
            for (String part : List.of(b.group(1) + "_ingot", b.group(1), b.group(1) + "_gem", "raw_" + b.group(1)))
                if (known.test(ns + ":" + part)) return new Object[] {ns + ":" + part, 9};
        }
        return null;
    }

    static double softMax(double a, double b) { return Math.pow(Math.pow(a, 8) + Math.pow(b, 8), 1.0 / 8); }

    static double dist(Map<String, double[]> regions, String a, String b) {
        double[] pa = regions.get(a), pb = regions.get(b);
        if (pa == null || pb == null) return 8000;
        return Math.hypot(pa[0] - pb[0], pa[1] - pb[1]);
    }

    /** Mid price of one unit in a region at the given stock (null: now). */
    public static double mid(State st, Map<String, double[]> regions, Function<String, Good> good, String region, String id, Double stock, int depth) {
        Good g = good.apply(id);
        if (g == null) return 0;
        Map<String, Double> held = st.stock.getOrDefault(region, Map.of());
        double s = Math.max(1, stock != null ? stock : held.getOrDefault(id, g.usual()));
        double raw = g.base() * Math.min(12, Math.max(0.01, Math.pow(g.usual() / s, 1 / elasticity(id))));
        String grp = group(id);
        if (grp != null) {
            double sum = 0; int n = 0;
            for (var e : held.entrySet()) {
                String other = e.getKey();
                Good go = good.apply(other);
                if (other.equals(id) || !grp.equals(group(other)) || go == null) continue;
                sum += Math.max(-3, Math.min(3, Math.log(Math.max(1, e.getValue()) / go.usual()))); n++;
            }
            if (n > 0) raw *= Math.exp(-SUBSTITUTION * sum / n);
        }
        double floor = g.base() * FLOOR_RAW;
        Object[] ins = depth < 3 ? inputs(id, k -> good.apply(k) != null) : null;
        if (ins != null) {
            double cost = (int) ins[1] * (LABOUR + mid(st, regions, good, region, (String) ins[0], null, depth + 1));
            floor = Math.max(floor, cost * FLOOR_MADE);
        }
        double p = softMax(raw, floor), adj = p;
        for (var r : st.stock.entrySet()) {
            if (r.getKey().equals(region) || !r.getValue().containsKey(id)) continue;
            double other = regionRaw(st, good, r.getKey(), id);
            double freight = FREIGHT * g.base() * dist(regions, region, r.getKey());
            if (p < other - freight) adj += (other - p - freight) / 2;
            else if (p > other + freight) adj -= (p - other - freight) / 2;
        }
        return adj;
    }

    static double regionRaw(State st, Function<String, Good> good, String region, String id) {
        Good g = good.apply(id);
        double s = Math.max(1, st.stock.getOrDefault(region, Map.of()).getOrDefault(id, g.usual()));
        return softMax(g.base() * Math.min(12, Math.max(0.01, Math.pow(g.usual() / s, 1 / elasticity(id)))), g.base() * FLOOR_RAW);
    }

    public static long buyPriceOf(double mid) { return (long) Math.ceil(mid * 1.15); }
    public static long sellPriceOf(double mid) { return (long) Math.floor(mid * 0.85); }

    /** Run n units through the market one at a time; scale is units of money per tw. */
    public static long trade(State st, Map<String, double[]> regions, Function<String, Good> good, String region, String id, int n, boolean buying, double scale) {
        Map<String, Double> held = st.held(region);
        held.putIfAbsent(id, good.apply(id).usual());
        long total = 0;
        for (int i = 0; i < n; i++) {
            double m = mid(st, regions, good, region, id, null, 0) * (scale == 0 ? 1 : scale);
            total += buying ? buyPriceOf(m) : sellPriceOf(m);
            held.merge(id, buying ? -1.0 : 1.0, Double::sum);
        }
        return total;
    }

    /** What n units would cost or fetch now, without moving the market. */
    public static long quote(State st, Map<String, double[]> regions, Function<String, Good> good, String region, String id, int n, boolean buying, double scale) {
        Map<String, Double> held = st.held(region);
        Double before = held.get(id);
        long t = trade(st, regions, good, region, id, n, buying, scale);
        if (before == null) held.remove(id); else held.put(id, before);
        return t;
    }
}
