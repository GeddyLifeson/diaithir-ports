package net.diaithir.mega.cleanroom;

/**
 * Clean-room drill-head tiers for mega diggers. Numbers are ours, from rock-cutting practice: a cutter can only work media up to
 * its hardness rating, cuts faster the harder it is, and wears in proportion to the hardness of what it cuts. No third-party code,
 * names tables or assets used.
 */
public enum DrillHead {
    //          rating (Mohs)  speed factor  life (m3 of rock-equivalent)
    IRON(4.0,   0.8,  4_000),
    STEEL(5.5,  1.0,  12_000),
    CARBIDE(8.5, 1.5, 60_000),
    DIAMOND(10.0, 2.0, 150_000);

    public final double mohs, speed, lifeM3;

    DrillHead(double mohs, double speed, double lifeM3) { this.mohs = mohs; this.speed = speed; this.lifeM3 = lifeM3; }

    /** Hardness of a medium on the Mohs-like scale used here. */
    public static double mediumMohs(String medium) {
        return switch (medium) {
            case "soil" -> 1.5;
            case "overburden" -> 2.5;
            case "ore" -> 5.0;
            case "rock" -> 6.5;
            case "regolith" -> 7.0;
            default -> throw new IllegalArgumentException("unknown medium " + medium);
        };
    }

    public boolean canCut(String medium) { return mohs >= mediumMohs(medium); }

    /** Dig-rate multiplier for this head on a medium, 0 when it cannot cut it. */
    public double rateFactor(String medium) { return canCut(medium) ? speed : 0.0; }

    /** Wear in fraction of life used digging {@code m3} of a medium (harder media wear faster). */
    public double wear(String medium, double m3) {
        return m3 * (mediumMohs(medium) / 6.5) / lifeM3;
    }
}
