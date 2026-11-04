package net.diaithir.vehicles.tracks;

/**
 * Skid-steer tracked drive, clean-room from how tracked vehicles behave (no Flan's Mod code, models or text): two tracks, each driven to a target speed.
 * Body speed is the mean of the two tracks, yaw rate is their difference over the track gauge, so equal commands drive straight, unequal ones turn, and opposite ones
 * pivot on the spot (slower, because the ground drags). Power limits how fast the tracks can speed up; with no fuel they only coast down. SI throughout.
 */
public final class TrackDrive {
    private static final double G = 9.81;

    /** Vehicle constants: mass kg, top speed m/s, speed-up and braking m/s^2, track gauge m, engine power W, efficiency 0..1, rolling resistance and turn scrub coefficients. */
    public record Spec(double massKg, double maxSpeed, double accel, double brake, double gauge, double powerW, double efficiency, double rolling, double scrub, double pivotFactor) {
        /** A 12 tonne light tank: 36 km/h, 300 kW diesel. */
        public static final Spec LIGHT_TANK = new Spec(12_000, 10.0, 2.5, 5.0, 2.4, 300_000, 0.35, 0.06, 0.35, 0.5);
    }

    /** One step's outcome: forward speed m/s, yaw rate rad/s (positive turns right), mechanical power W drawn. */
    public record Out(double speed, double yawRate, double powerW) {}

    private final Spec spec;
    private double left;
    private double right;

    public TrackDrive(Spec spec) {
        this.spec = spec;
    }

    public double leftSpeed() {
        return left;
    }

    public double rightSpeed() {
        return right;
    }

    public void stop() {
        left = right = 0;
    }

    private static double clamp(double v) {
        return Math.max(-1, Math.min(1, v));
    }

    private double approach(double v, double target, double dt, double accelLimit) {
        double dv = target - v;
        double rate = (Math.abs(target) < Math.abs(v) || target * v < 0) ? spec.brake() : accelLimit;
        double step = rate * dt;
        return Math.abs(dv) <= step ? target : v + Math.copySign(step, dv);
    }

    /** @param throttle -1..1 forward positive; @param steer -1..1 right positive; @param fuel false when the tank is dry. */
    public Out step(double throttle, double steer, double dt, boolean fuel) {
        double cl = clamp(throttle + steer), cr = clamp(throttle - steer);
        double m = Math.max(1, Math.max(Math.abs(throttle + steer), Math.abs(throttle - steer)));
        cl = (throttle + steer) / m;
        cr = (throttle - steer) / m;
        if (Math.abs(throttle) < 0.05) { // pivoting: the ground drags, so slower
            cl *= spec.pivotFactor();
            cr *= spec.pivotFactor();
        }
        double tl = fuel ? cl * spec.maxSpeed() : 0, tr = fuel ? cr * spec.maxSpeed() : 0;
        double coast = fuel ? 1.0 : 0.3; // a dead engine brakes only by drag
        // power-limited speed-up: estimate demand at the proposed change, scale the change down if the engine cannot supply it
        double a = spec.accel() * coast;
        double nl = approach(left, tl, dt, a), nr = approach(right, tr, dt, a);
        double demand = demand(left, right, nl, nr, dt);
        if (fuel && demand > spec.powerW()) {
            double f = Math.max(0, spec.powerW() - demand(left, right, left, right, dt)) / Math.max(1e-9, demand - demand(left, right, left, right, dt));
            nl = left + (nl - left) * f;
            nr = right + (nr - right) * f;
            demand = spec.powerW();
        }
        left = nl;
        right = nr;
        double v = (left + right) / 2.0;
        double w = (left - right) / spec.gauge();
        return new Out(v, w, fuel ? demand : 0);
    }

    private double demand(double l0, double r0, double l1, double r1, double dt) {
        double v1 = (l1 + r1) / 2.0, v0 = (l0 + r0) / 2.0;
        double w1 = (l1 - r1) / spec.gauge();
        double roll = spec.rolling() * spec.massKg() * G * Math.abs(v1);
        double acc = Math.max(0, spec.massKg() * Math.abs(v1) * (Math.abs(v1) - Math.abs(v0)) / dt);
        double scrub = spec.scrub() * spec.massKg() * G * Math.abs(w1) * spec.gauge() / 2.0;
        return roll + acc + scrub;
    }
}
