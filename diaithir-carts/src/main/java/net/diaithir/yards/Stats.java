package net.diaithir.yards;

import java.util.Map;
import java.util.TreeMap;

/** Summed result of a build. hp is the weakest structural part. */
public record Stats(double massKg, double powerW, double thrustN, double buoyancyN, double capacityL, int crewSlots, double hp,
                    Map<String, Double> extra) {
    public double thrustToWeight(double g) {
        return massKg <= 0 ? 0 : thrustN / (massKg * g);
    }

    static Stats sum(Iterable<PartDef> parts) {
        double m = 0, p = 0, t = 0, b = 0, c = 0, hp = Double.MAX_VALUE;
        int crew = 0;
        Map<String, Double> ex = new TreeMap<>();
        boolean any = false;
        for (PartDef d : parts) {
            any = true;
            m += d.massKg();
            p += d.powerW();
            t += d.thrustN();
            b += d.buoyancyN();
            c += d.capacityL();
            crew += d.crewSlots();
            if (d.hp() > 0) hp = Math.min(hp, d.hp());
            d.extra().forEach((k, v) -> ex.merge(k, v, Double::sum));
        }
        return new Stats(m, p, t, b, c, crew, any && hp != Double.MAX_VALUE ? hp : 0, Map.copyOf(ex));
    }
}
