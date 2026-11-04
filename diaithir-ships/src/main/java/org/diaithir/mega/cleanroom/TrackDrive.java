package org.diaithir.mega.cleanroom;

/**
 * Clean-room crawler track drive from vehicle mechanics (no third-party code or assets). Two tracks, differential steering.
 * Units SI: kg, m, s, N, W.
 */
public final class TrackDrive {
    public static final double G = 9.81;

    private final double massKg, powerW, trackAreaM2, trackGaugeM, maxTractionCoef, rollingCoef;

    /** @param trackAreaM2 ground contact area of both tracks together */
    public TrackDrive(double massKg, double powerW, double trackAreaM2, double trackGaugeM, double maxTractionCoef, double rollingCoef) {
        this.massKg = massKg; this.powerW = powerW; this.trackAreaM2 = trackAreaM2; this.trackGaugeM = trackGaugeM;
        this.maxTractionCoef = maxTractionCoef; this.rollingCoef = rollingCoef;
    }

    /** Mean ground pressure in kPa. */
    public double groundPressureKPa() { return massKg * G / trackAreaM2 / 1000.0; }

    /** Top speed in m/s on level ground: drive power minus rolling resistance work, capped by traction-limited force at low speed. */
    public double topSpeed() { return powerW / (rollingCoef * massKg * G); }

    /** Tractive force available at speed v (m/s): min(power/v, traction limit). */
    public double tractiveForce(double v) {
        double limit = maxTractionCoef * massKg * G;
        return v <= 0 ? limit : Math.min(powerW / v, limit);
    }

    /** Steepest climbable slope (rise over run) at crawl speed. */
    public double maxGradient() {
        double f = maxTractionCoef - rollingCoef;
        return f <= 0 ? 0 : Math.tan(Math.asin(Math.min(1.0, f)));
    }

    /** Yaw rate rad/s from left and right track speeds m/s (differential steering). */
    public double yawRate(double vLeft, double vRight) { return (vRight - vLeft) / trackGaugeM; }

    /** Forward speed m/s of the hull centre. */
    public double forwardSpeed(double vLeft, double vRight) { return (vLeft + vRight) / 2.0; }

    /** True when the ground can carry the vehicle (bearing capacity in kPa, e.g. soft soil ~ 50). */
    public boolean ground(double bearingKPa) { return groundPressureKPa() <= bearingKPa; }
}
