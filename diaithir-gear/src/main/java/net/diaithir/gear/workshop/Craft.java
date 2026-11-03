package net.diaithir.gear.workshop;

import java.util.List;
import java.util.Map;

/**
 * Pure assembly maths for the parts stations (no Minecraft types, so it is unit-testable).
 * Units are SI: volume in litres, density in kg/L, mass in kg, hardness and toughness as 0..10 scores.
 */
public final class Craft {
    /** A material that can fill roles (head, handle, barrel...). */
    public record Material(String id, int age, double hardness, double toughness, double density, double flex, int color,
                           List<String> roles, List<String> items) {}

    /** One part slot of a blueprint: its role, how many items it eats, the volume it takes and how much each stat leans on it. */
    public record Slot(String name, String role, int count, double litres, double damage, double durability, double speed) {}

    /** What a station can build. kind: melee, tool, ranged, gun, kitchen. */
    public record Blueprint(String id, String station, String base, int age, String kind, double baseDamage,
                            double baseSpeed, int baseDurability, double refMassKg, int improvementSlots, boolean schematic, String model, List<Slot> slots) {}

    /** Result of an assembly. damage is the total attack damage and speed the attack-speed modifier (total minus 4), mass is for tooltips and recoil. */
    public record Stats(double damage, double speed, int durability, double massKg, double spreadMul) {}

    public static final int[] HONE_USES = {100, 300, 700, 1500, 3100};
    public static final double HONE_STEP = 0.05;

    private Craft() {}

    /** Slots with no material yet, in blueprint order. */
    public static List<Slot> missing(Blueprint b, Map<String, Material> parts) {
        return b.slots().stream().filter(s -> !parts.containsKey(s.name())).toList();
    }

    /** Honing level 0..5 from how often the item has been used. */
    public static int honeLevel(int uses) {
        int n = 0;
        for (int t : HONE_USES) if (uses >= t) n++;
        return n;
    }

    /** Highest Age among the blueprint and the chosen materials; a player needs at least this Age. */
    public static int requiredAge(Blueprint b, Map<String, Material> parts) {
        int a = b.age();
        for (Material m : parts.values()) a = Math.max(a, m.age());
        return a;
    }

    /**
     * Combine parts into stats. A slot's weight says how much that slot decides a stat: a head decides damage,
     * a handle decides speed and some durability. Material score 5 is neutral (x1), 10 is x1.5, 0 is x0.5.
     */
    public static Stats assemble(Blueprint b, Map<String, Material> parts, int honeLevel, double improvementDamage) {
        double dmg = 0, dur = 0, spd = 0, wd = 0, wu = 0, ws = 0, mass = 0, flexSum = 0;
        for (Slot s : b.slots()) {
            Material m = parts.get(s.name());
            if (m == null) throw new IllegalArgumentException("slot " + s.name() + " empty");
            dmg += s.damage() * lean(m.hardness());
            dur += s.durability() * lean(m.toughness());
            spd += s.speed() * lean(m.flex());
            wd += s.damage(); wu += s.durability(); ws += s.speed();
            mass += s.litres() * m.density();
            flexSum += m.flex() * s.litres();
        }
        double hone = 1 + HONE_STEP * honeLevel;
        double damage = b.baseDamage() * (wd > 0 ? dmg / wd : 1) * hone + improvementDamage;
        int durability = (int) Math.max(1, Math.round(b.baseDurability() * (wu > 0 ? dur / wu : 1) * (1 + 0.5 * HONE_STEP * honeLevel)));
        double handling = ws > 0 ? spd / ws : 1;
        double heavy = b.refMassKg() > 0 ? Math.sqrt(b.refMassKg() / Math.max(0.05, mass)) : 1;
        double speed = (4 + b.baseSpeed()) * clamp(heavy, 0.6, 1.4) * (0.8 + 0.2 * handling) - 4;
        double spread = clamp(1.0 / lean(flexSum / Math.max(0.01, b.slots().stream().mapToDouble(Slot::litres).sum())), 0.6, 1.6);
        return new Stats(round(damage), round(speed), durability, round(mass), round(spread));
    }

    private static double lean(double score) { return 0.5 + clamp(score, 0, 10) / 10.0; }

    private static double clamp(double v, double lo, double hi) { return Math.max(lo, Math.min(hi, v)); }

    private static double round(double v) { return Math.round(v * 100.0) / 100.0; }
}
