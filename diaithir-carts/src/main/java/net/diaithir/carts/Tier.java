package net.diaithir.carts;

/** The three rail tiers. maxSpeed is blocks per tick (vanilla carts are capped at 0.4); boost is what a powered rail of the tier adds per tick. */
public enum Tier {
    BRONZE("bronze", 0.6, 0.09, Gauge.STANDARD),
    STEEL("steel", 0.9, 0.135, Gauge.STANDARD),
    FAST("fast", 1.2, 0.18, Gauge.STANDARD),
    /** Narrow-gauge rail: slower than any standard-gauge tier, but cheap and takes curves and grades. */
    NARROW("narrow", 0.5, 0.075, Gauge.NARROW),
    /** Both gauges on one track: the narrow cap, since the shared rail is laid to the weaker standard. */
    DUAL("dual", 0.5, 0.075, Gauge.BOTH);

    public final String id;
    public final double maxSpeed;
    public final double boost;
    public final Gauge gauge;

    Tier(String id, double maxSpeed, double boost, Gauge gauge) {
        this.gauge = gauge;
        this.id = id;
        this.maxSpeed = maxSpeed;
        this.boost = boost;
    }
}
