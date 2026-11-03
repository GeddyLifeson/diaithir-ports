package net.diaithir.yards;

import java.util.Map;

/** A catalog part. All numbers SI. Heavy parts (>= 500 kg) need a crane to move. */
public record PartDef(String item, String kind, double massKg, double hp, double powerW, double thrustN,
                      double buoyancyN, double capacityL, int crewSlots, int minAge, Map<String, Double> extra, String block) {
    public static final double HEAVY_KG = 500;

    /** Source-compatible constructor without block (registry id of the block placed when installed). */
    public PartDef(String item, String kind, double massKg, double hp, double powerW, double thrustN, double buoyancyN,
                   double capacityL, int crewSlots, int minAge, Map<String, Double> extra) {
        this(item, kind, massKg, hp, powerW, thrustN, buoyancyN, capacityL, crewSlots, minAge, extra, "");
    }

    public PartDef block(String b) {
        return new PartDef(item, kind, massKg, hp, powerW, thrustN, buoyancyN, capacityL, crewSlots, minAge, extra, b);
    }

    public boolean heavy() {
        return massKg >= HEAVY_KG;
    }

    public static PartDef of(String item, String kind, double massKg) {
        return new PartDef(item, kind, massKg, 0, 0, 0, 0, 0, 0, 0, Map.of());
    }

    public PartDef power(double w) {
        return new PartDef(item, kind, massKg, hp, w, thrustN, buoyancyN, capacityL, crewSlots, minAge, extra, block);
    }

    public PartDef thrust(double n) {
        return new PartDef(item, kind, massKg, hp, powerW, n, buoyancyN, capacityL, crewSlots, minAge, extra, block);
    }

    public PartDef hp(double h) {
        return new PartDef(item, kind, massKg, h, powerW, thrustN, buoyancyN, capacityL, crewSlots, minAge, extra, block);
    }

    public PartDef age(int a) {
        return new PartDef(item, kind, massKg, hp, powerW, thrustN, buoyancyN, capacityL, crewSlots, a, extra, block);
    }
}
