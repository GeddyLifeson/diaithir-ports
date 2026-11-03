package net.diaithir.carts;


/**
 * The two track gauges. STANDARD is 1435 mm (4 ft 8.5 in), the only gauge for the US and UK main lines. NARROW is one narrow gauge, 3 ft (914 mm), the gauge of the main US narrow-gauge lines (Denver and Rio Grande, East Broad Top, White Pass) and the Isle of Man,
 * which every US and UK narrow-gauge design (2 ft to 3 ft 6 in in life) is folded onto. They do not interconnect: a vehicle only moves on its own
 * gauge, on a dual-gauge rail (BOTH) and on gauge-neutral rails (ANY: vanilla, station, routing rails).
 * Trade-offs: narrow is cheap and light, takes tight curves and steep grades, but is slow and limited in axle load, payload and power;
 * standard is fast and heavy and carries more, but loses speed in curves and on grades and costs more.
 */
public enum Gauge {
    ANY("any", 1.0, 1.0, 1.0, Integer.MAX_VALUE, 1.0),
    STANDARD("standard", 0.57, 0.96, 1.0, Integer.MAX_VALUE, 1.0),
    NARROW("narrow", 0.85, 0.98, 0.4, 90_000, 0.6),
    BOTH("both", 0.57, 0.96, 1.0, Integer.MAX_VALUE, 1.0);

    public final String id;
    /** Share of the rail's speed cap a cart keeps on a curved rail. */
    public final double curveSpeed;
    /** Speed a cart keeps each tick on an ascending rail. */
    public final double gradeKeep;
    /** Share of the standard payload a freight vehicle of this gauge carries. */
    public final double payload;
    /** Heaviest empty vehicle (kg) the track takes. */
    public final int maxMassKg;
    /** Share of the standard rail cost, for the generators. */
    public final double cost;

    Gauge(String id, double curveSpeed, double gradeKeep, double payload, int maxMassKg, double cost) {
        this.id = id;
        this.curveSpeed = curveSpeed;
        this.gradeKeep = gradeKeep;
        this.payload = payload;
        this.maxMassKg = maxMassKg;
        this.cost = cost;
    }

    public static Gauge of(String id) {
        for (Gauge g : values()) if (g.id.equals(id)) return g;
        return ANY;
    }

    /** True when a vehicle of gauge vehicle may run on a rail of gauge rail. */
    public static boolean runs(Gauge vehicle, Gauge rail) {
        return vehicle == ANY || rail == ANY || rail == BOTH || vehicle == rail;
    }
}
