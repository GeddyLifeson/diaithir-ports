package net.diaithir.animation.machines;

/** Part transform: rotation in degrees, offset in blocks (1 = 16 px), uniform scale. Minecraft-free. */
public record Pose(float rx, float ry, float rz, float ox, float oy, float oz, float scale) {
    public static final Pose NONE = new Pose(0, 0, 0, 0, 0, 0, 1);

    public Pose lerp(Pose o, float t) {
        return new Pose(l(rx, o.rx, t), l(ry, o.ry, t), l(rz, o.rz, t),
                l(ox, o.ox, t), l(oy, o.oy, t), l(oz, o.oz, t), l(scale, o.scale, t));
    }

    private static float l(float a, float b, float t) { return a + (b - a) * t; }
}
