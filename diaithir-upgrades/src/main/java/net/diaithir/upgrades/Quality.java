package net.diaithir.upgrades;

/** Item and block quality tiers. Higher quality scales module and machine effects. */
public enum Quality {
    NORMAL, UNCOMMON, RARE, EPIC, LEGENDARY;

    /** Effect multiplier: 1.0, 1.3, 1.6, 1.9, 2.5. Own numbers, not copied from any game. */
    public double scale() {
        return this == LEGENDARY ? 2.5 : 1.0 + 0.3 * ordinal();
    }

    public Quality next() {
        return values()[Math.min(ordinal() + 1, values().length - 1)];
    }
}
