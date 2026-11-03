package net.diaithir.yards;

import java.util.Map;

/** A catalog part. All numbers SI. Heavy parts (>= 500 kg) need a crane to move. */
public record PartDef(String item, String kind, double massKg, double hp, double powerW, double thrustN,
                      double buoyancyN, double capacityL, int crewSlots, int minAge, Map<String, Double> extra) {
    public static final double HEAVY_KG = 500;

    public boolean heavy() {
        return massKg >= HEAVY_KG;
    }

    public static PartDef of(String item, String kind, double massKg) {
        return new PartDef(item, kind, massKg, 0, 0, 0, 0, 0, 0, 0, Map.of());
    }

    public PartDef power(double w) {
        return new PartDef(item, kind, massKg, hp, w, thrustN, buoyancyN, capacityL, crewSlots, minAge, extra);
    }

    public PartDef thrust(double n) {
        return new PartDef(item, kind, massKg, hp, powerW, n, buoyancyN, capacityL, crewSlots, minAge, extra);
    }

    public PartDef hp(double h) {
        return new PartDef(item, kind, massKg, h, powerW, thrustN, buoyancyN, capacityL, crewSlots, minAge, extra);
    }

    public PartDef age(int a) {
        return new PartDef(item, kind, massKg, hp, powerW, thrustN, buoyancyN, capacityL, crewSlots, a, extra);
    }
}
