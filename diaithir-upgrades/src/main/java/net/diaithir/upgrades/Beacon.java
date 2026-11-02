package net.diaithir.upgrades;

import java.util.List;

/**
 * A beacon holds modules and shares part of their speed, efficiency and quality effects (never
 * productivity) with blocks in range. Several beacons stack with diminishing return 1/sqrt(n).
 */
public final class Beacon {
    public static final double TRANSMISSION = 0.5;

    private final Slotted slots;
    private final int range;

    public Beacon(Target target, int range) {
        this.slots = new Slotted(target);
        this.range = range;
    }

    public Slotted slots() { return slots; }
    public int range() { return range; }

    public boolean covers(int dx, int dy, int dz) {
        return Math.abs(dx) <= range && Math.abs(dy) <= range && Math.abs(dz) <= range;
    }

    public Effects shared() {
        Effects e = slots.own();
        return new Effects(e.speed(), 0, e.consumption(), e.quality()).times(TRANSMISSION);
    }

    /** Beacon bonus a block receives from every beacon covering it. */
    public static Effects combine(List<Beacon> covering) {
        if (covering.isEmpty()) return Effects.NONE;
        Effects sum = Effects.NONE;
        for (Beacon b : covering) sum = sum.plus(b.shared());
        return sum.times(1.0 / Math.sqrt(covering.size()));
    }
}
