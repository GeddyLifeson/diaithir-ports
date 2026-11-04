package net.diaithir.entityanimation.betteranimations;

/**
 * Motion helpers for the Better Animations Collection feature (clean-room, no Minecraft types so they can be checked without a client).
 * Angles are radians, time in seconds.
 */
public final class Motion {
    private Motion() {}

    /** Spring state: position and velocity of one animated value. */
    public static final class State {
        public double pos, vel;
        public State(double pos) { this.pos = pos; }
    }

    /**
     * Critically damped spring step toward target; omega is the natural frequency in rad/s (about 8 is quick, 3 lazy).
     * Exact solution of the damped system, so it is stable for any dt.
     */
    public static void spring(State s, double target, double omega, double dt) {
        double x = s.pos - target;
        double e = Math.exp(-omega * dt);
        double tmp = (s.vel + omega * x) * dt;
        s.pos = target + (x + tmp) * e;
        s.vel = (s.vel - omega * tmp) * e;
    }

    /** Sine wobble with an entity specific phase. */
    public static double wobble(double timeSeconds, double amplitude, double hz, int entityId) {
        double phase = (entityId * 0.6180339887498949 % 1.0) * 2.0 * Math.PI;
        return amplitude * Math.sin(2.0 * Math.PI * hz * timeSeconds + phase);
    }

    /** Damped oscillation after an impulse at t=0 (landing squash): amplitude decays with the given half-life in seconds. */
    public static double jiggle(double sinceImpulse, double amplitude, double hz, double halfLife) {
        if (sinceImpulse < 0) return 0.0;
        double decay = Math.pow(0.5, sinceImpulse / halfLife);
        return amplitude * decay * Math.cos(2.0 * Math.PI * hz * sinceImpulse);
    }

    /** Amplitude scale from the config intensity and distance (fades to nothing at max distance). */
    public static double scale(double intensity, double distance, double maxDistance) {
        if (distance >= maxDistance) return 0.0;
        double fade = distance <= maxDistance * 0.5 ? 1.0 : (maxDistance - distance) / (maxDistance * 0.5);
        return Math.max(0.0, intensity) * fade;
    }
}
