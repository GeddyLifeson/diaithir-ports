package net.diaithir.upgrades;

import java.util.List;

/** Final effect for one block: own modules plus beacons, clamped. Glue calls this once per change. */
public final class Resolver {
    private Resolver() {}

    public static Effects resolve(Slotted block, List<Beacon> covering) {
        return block.own().plus(Beacon.combine(covering)).clamped();
    }

    /** Output quality after a craft, given a roll in [0,1): quality chance may lift it one tier. */
    public static Quality rollQuality(Quality base, Effects e, double roll, int maxQuality) {
        return roll < e.clamped().quality() && base.ordinal() < maxQuality ? base.next() : base;
    }
}
