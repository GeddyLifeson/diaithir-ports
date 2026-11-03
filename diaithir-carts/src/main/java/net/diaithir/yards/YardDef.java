package net.diaithir.yards;

import java.util.List;

/** Data definition of a yard: where it can stand, what stages it has and what it rolls out. */
public record YardDef(String id, Tier tier, Site site, List<String> dimensions, String result, List<Stage> stages, double craneSpeedup,
                Footprint footprint) {
    /** Width, length, height in blocks. */
    public record Footprint(int w, int l, int h) {}

    /** Source-compatible constructor without footprint. */
    public YardDef(String id, Tier tier, Site site, List<String> dimensions, String result, List<Stage> stages, double craneSpeedup) {
        this(id, tier, site, dimensions, result, stages, craneSpeedup, new Footprint(0, 0, 0));
    }

    public enum Tier {
        WORKSHOP(1), ERECTING_SHOP(2), ASSEMBLY_LINE(4), AUTOMATED(8);
        public final double speed;
        Tier(double speed) { this.speed = speed; }
    }

    public enum Site { GROUND, WATER, ORBITAL }

    /** One slot group: {@code count} parts of {@code kind}. */
    public record Slot(String kind, int count) {}

    public record Stage(String id, List<Slot> slots, int baseTicks, String ghost) {}

    public boolean allowedIn(String dimension) {
        return dimensions.isEmpty() || dimensions.contains(dimension);
    }
}
