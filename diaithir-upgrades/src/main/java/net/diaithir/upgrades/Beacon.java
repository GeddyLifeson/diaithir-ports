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

    /** Wrap modules that already live elsewhere (a beacon block entity's saved slots). */
    public static Beacon wrap(Slotted slots, int range) { return new Beacon(slots, range); }

    private Beacon(Slotted slots, int range) {
        this.slots = slots;
        this.range = range;
    }

    public Beacon(Target target, int range) {
        this(new Slotted(target), range);
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
