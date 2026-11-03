package net.diaithir.units;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Registry and converter for every unit in the pack. Canonical units: J, W, L, K, bar, N·m, rpm, kg, m, s, g Ag.
 * 1 FE (NeoForge transfer unit) is exactly 1 J, and 1 mB is exactly 1 L, so existing FE and fluid handlers need no change.
 * Third-party units are registered with the ratio the pack uses for them; see plans/UNITS.md for the reasons.
 */
public final class Units {
    private Units() {}

    private static final Map<String, Unit> ALL = new LinkedHashMap<>();

    public static final double TICKS_PER_SECOND = 20;

    public static Unit register(Unit u) {
        ALL.put(u.id(), u);
        return u;
    }

    public static Unit get(String id) {
        Unit u = ALL.get(id);
        if (u == null) throw new IllegalArgumentException("unknown unit " + id);
        return u;
    }

    public static boolean has(String id) {
        return ALL.containsKey(id);
    }

    public static Iterable<Unit> all() {
        return ALL.values();
    }

    public static double convert(double value, Unit from, Unit to) {
        if (from.dim() != to.dim()) throw new IllegalArgumentException(from.id() + " to " + to.id() + ": different quantities");
        return to.fromCanonical(from.toCanonical(value));
    }

    public static double convert(double value, String from, String to) {
        return convert(value, get(from), get(to));
    }

    /** Whole canonical units for a foreign amount, rounded down so conversion never creates energy or fluid. */
    public static long toCanonicalFloor(long amount, String from) {
        return (long) Math.floor(get(from).toCanonical(amount) + 1e-9);
    }

    /** Whole foreign units that a canonical amount can pay for, rounded down. */
    public static long fromCanonicalFloor(long canonical, String to) {
        return (long) Math.floor(get(to).fromCanonical(canonical) + 1e-9);
    }

    private static void r(String id, Dim d, String sym, double f) { register(new Unit(id, d, sym, f)); }

    static {
        // Energy, canonical J. 1 FE = 1 J.
        r("j", Dim.ENERGY, "J", 1);
        r("kj", Dim.ENERGY, "kJ", 1e3);
        r("mj", Dim.ENERGY, "MJ", 1e6);
        r("gj", Dim.ENERGY, "GJ", 1e9);
        r("neoforge:fe", Dim.ENERGY, "FE", 1);
        r("thermal:rf", Dim.ENERGY, "RF", 1);
        r("mekanism:joule", Dim.ENERGY, "J(Mek)", 2.5);
        r("ic2:eu", Dim.ENERGY, "EU", 4);
        r("gtceu:eu", Dim.ENERGY, "EU", 4);
        r("ae2:ae", Dim.ENERGY, "AE", 2);
        r("buildcraft:mj", Dim.ENERGY, "MJ(BC)", 10);
        // Magic, folded into energy: one magic quantum is 100 J (Source is ten of them).
        r("botania:mana", Dim.ENERGY, "mana", 100);
        r("naturesaura:aura", Dim.ENERGY, "aura", 100);
        r("embers:ember", Dim.ENERGY, "ember", 100);
        r("bloodmagic:lp", Dim.ENERGY, "LP", 100);
        r("thaumcraft:vis", Dim.ENERGY, "vis", 100);
        r("arsnouveau:source", Dim.ENERGY, "source", 1000);
        // Power, canonical W. 20 ticks = 1 s, so 1 FE/t = 20 W.
        r("w", Dim.POWER, "W", 1);
        r("kw", Dim.POWER, "kW", 1e3);
        r("mw", Dim.POWER, "MW", 1e6);
        r("neoforge:fe_per_tick", Dim.POWER, "FE/t", TICKS_PER_SECOND);
        r("mekanism:joule_per_tick", Dim.POWER, "J(Mek)/t", 2.5 * TICKS_PER_SECOND);
        r("ic2:eu_per_tick", Dim.POWER, "EU/t", 4 * TICKS_PER_SECOND);
        // Volume, canonical L. 1 mB = 1 L, 1 bucket = 1000 L = 1 m3 (one block is one metre cubed).
        r("l", Dim.VOLUME, "L", 1);
        r("m3", Dim.VOLUME, "m³", 1000);
        r("neoforge:mb", Dim.VOLUME, "mB", 1);
        r("neoforge:bucket", Dim.VOLUME, "bucket", 1000);
        r("fabric:droplet", Dim.VOLUME, "droplet", 1.0 / 81);
        // Temperature, canonical K.
        r("k", Dim.TEMPERATURE, "K", 1);
        register(new Unit("c", Dim.TEMPERATURE, "°C", 1, 273.15));
        register(new Unit("pneumaticcraft:heat", Dim.TEMPERATURE, "°C", 1, 273.15));
        r("mekanism:heat_k", Dim.TEMPERATURE, "K", 1);
        // Pressure, canonical bar.
        r("bar", Dim.PRESSURE, "bar", 1);
        r("kpa", Dim.PRESSURE, "kPa", 0.01);
        r("pa", Dim.PRESSURE, "Pa", 1e-5);
        r("pneumaticcraft:bar", Dim.PRESSURE, "bar", 1);
        // Torque and rotation. Create stress units are newton metres.
        r("n_m", Dim.TORQUE, "N·m", 1);
        r("create:su", Dim.TORQUE, "SU", 1);
        r("rpm", Dim.ROTATION, "rpm", 1);
        r("rad_s", Dim.ROTATION, "rad/s", 60 / (2 * Math.PI));
        // Mass, length, time, currency.
        r("kg", Dim.MASS, "kg", 1);
        r("g", Dim.MASS, "g", 1e-3);
        r("m", Dim.LENGTH, "m", 1);
        r("km", Dim.LENGTH, "km", 1e3);
        r("s", Dim.TIME, "s", 1);
        r("min", Dim.TIME, "min", 60);
        r("minecraft:tick", Dim.TIME, "tick", 1.0 / TICKS_PER_SECOND);
        r("g_ag", Dim.CURRENCY, "g Ag", 1);
        r("denarius", Dim.CURRENCY, "denarius", 3.4);
    }
}
