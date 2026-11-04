package net.diaithir.worldsys.aircrafttuning;

import java.util.Map;

/** Tuning constants and lookup for aircraft types. */
public final class AircraftTuning {
    public record Tune(float speed, float lift, float fuelBurn, float stressPerSecond) {
        public float scaleSpeed(float base) {
            return clamp(base * speed, 0.0F, 4.0F);
        }

        public float scaleLift(float base) {
            return clamp(base * lift, 0.0F, 4.0F);
        }

        public float scaleFuel(float base) {
            return Math.max(0.0F, base * fuelBurn);
        }
    }

    public static final Tune DEFAULT = new Tune(1.0F, 1.0F, 1.0F, 0.0F);

    private static final Map<String, Tune> TYPES = Map.of(
            "airship_light", new Tune(1.1F, 0.9F, 0.8F, 0.0F),
            "airship_heavy", new Tune(0.8F, 1.3F, 1.4F, 0.0F),
            "biplane_racer", new Tune(1.35F, 1.0F, 1.2F, 0.0F),
            "gyrodyne", new Tune(0.9F, 1.2F, 1.0F, 64.0F),
            "quadrocopter", new Tune(0.85F, 1.4F, 1.1F, 128.0F),
            "cargo_blimp", new Tune(0.6F, 1.6F, 1.5F, 256.0F));

    private AircraftTuning() {
    }

    public static Tune get(String typeId) {
        int colon = typeId.indexOf(':');
        String path = colon >= 0 ? typeId.substring(colon + 1) : typeId;
        return TYPES.getOrDefault(path, DEFAULT);
    }

    public static float clamp(float v, float min, float max) {
        return Math.max(min, Math.min(max, v));
    }
}
