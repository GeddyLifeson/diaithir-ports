package net.diaithir.carts;

/** The three rail tiers. maxSpeed is blocks per tick (vanilla carts are capped at 0.4); boost is what a powered rail of the tier adds per tick. */
public enum Tier {
    BRONZE("bronze", 0.6, 0.09),
    STEEL("steel", 0.9, 0.135),
    FAST("fast", 1.2, 0.18);

    public final String id;
    public final double maxSpeed;
    public final double boost;

    Tier(String id, double maxSpeed, double boost) {
        this.id = id;
        this.maxSpeed = maxSpeed;
        this.boost = boost;
    }
}
