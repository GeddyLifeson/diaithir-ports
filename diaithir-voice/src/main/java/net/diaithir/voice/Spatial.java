package net.diaithir.voice;

/** Distance falloff and stereo placement. Pure maths so it can be tested without a game. */
public final class Spatial {
    private Spatial() {}

    /** 1 inside {@code near}, 0 beyond {@code range}, a squared linear ramp between. */
    public static float gain(double distance, double near, double range) {
        if (range <= near) return distance <= range ? 1f : 0f;
        if (distance <= near) return 1f;
        if (distance >= range) return 0f;
        double t = 1.0 - (distance - near) / (range - near);
        return (float) (t * t);
    }

    public static int quantise(float gain) {
        return Math.round(Math.max(0f, Math.min(1f, gain)) * 255f);
    }

    /**
     * Pan from -1 (hard left) to 1 (hard right) for a source at (dx, dz) relative to a listener facing
     * {@code yawDegrees} (Minecraft yaw: 0 faces +Z, 90 faces -X). A source on top of the listener is centred.
     */
    public static float pan(double dx, double dz, float yawDegrees) {
        double len = Math.sqrt(dx * dx + dz * dz);
        if (len < 0.5) return 0f;
        double yaw = Math.toRadians(yawDegrees);
        double rightX = -Math.cos(yaw), rightZ = -Math.sin(yaw);
        double p = (dx * rightX + dz * rightZ) / len;
        return (float) Math.max(-1.0, Math.min(1.0, p));
    }

    /** Constant power left and right gains for a pan value. */
    public static float[] stereo(float pan) {
        double a = (Math.max(-1f, Math.min(1f, pan)) + 1.0) * Math.PI / 4.0;
        return new float[] {(float) Math.cos(a), (float) Math.sin(a)};
    }
}
