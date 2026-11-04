package net.diaithir.worldsys.vehicletuning;

/** Tuning constants and clamped profile record for external vehicles. */
public final class VehicleTuning {
    public static final double MIN_MULT = 0.1D;
    public static final double MAX_MULT = 4.0D;
    public static final double DEFAULT_SPEED = 1.0D;
    public static final double DEFAULT_FUEL_BURN = 1.0D;
    public static final double DEFAULT_DAMAGE = 1.0D;
    public static final double DEFAULT_REPAIR = 1.0D;

    public record Profile(double speed, double fuelBurn, double damage, double repair) {
        public static Profile of(double speed, double fuelBurn, double damage, double repair) {
            return new Profile(clamp(speed), clamp(fuelBurn), clamp(damage), clamp(repair));
        }

        public static final Profile DEFAULT = of(DEFAULT_SPEED, DEFAULT_FUEL_BURN, DEFAULT_DAMAGE, DEFAULT_REPAIR);
        public static final Profile BOOSTED = of(1.15D, 0.9D, 0.85D, 1.25D);
    }

    public static double clamp(double v) {
        return Math.max(MIN_MULT, Math.min(MAX_MULT, v));
    }

    private VehicleTuning() {}
}
