package net.diaithir.portable;

/** Storage tiers: slot counts and the number of upgrade slots each one carries. */
public enum Tier {
    WOOD("wood", 27, 1, 9),
    IRON("iron", 54, 2, 9),
    GOLD("gold", 81, 3, 9),
    DIAMOND("diamond", 108, 4, 12),
    NETHERITE("netherite", 162, 5, 18);

    public final String id;
    public final int slots;
    public final int upgradeSlots;
    public final int columns;

    Tier(String id, int slots, int upgradeSlots, int columns) {
        this.id = id;
        this.slots = slots;
        this.upgradeSlots = upgradeSlots;
        this.columns = columns;
    }

    public int rows() {
        return (slots + columns - 1) / columns;
    }

    public static Tier byOrdinal(int i) {
        Tier[] v = values();
        return v[Math.floorMod(i, v.length)];
    }
}
