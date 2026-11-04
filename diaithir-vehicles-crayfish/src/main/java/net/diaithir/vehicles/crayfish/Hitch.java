// SPDX-License-Identifier: GPL-3.0-only
// Folded from MrCrayfish's Vehicle Mod (Unofficial 1.20.1 port by bay4lly; original by MrCrayfish, GPL). Rewritten for NeoForge 26.1.2; art is not used.
package net.diaithir.vehicles.crayfish;

/** Trailer hitch geometry (from the trailer entity's pulling motion): a trailer hangs a fixed hitch length behind its tow bar, faces it, and tilts to the slope. Pure maths, no Minecraft types. */
public final class Hitch {
    private Hitch() {}

    /** New trailer position (x, z), yaw and pitch in degrees. */
    public record Pose(double x, double z, float yaw, float pitch) {}

    /**
     * @param tow          tow bar position
     * @param trailer      trailer position
     * @param hitchBlocks  signed hitch length in blocks along the trailer's facing; negative puts the trailer behind the bar (the usual case)
     * @param pitchNow     current pitch in degrees
     */
    public static Pose follow(double tx, double ty, double tz, double x, double y, double z, double hitchBlocks, float pitchNow) {
        float yaw = (float) Math.toDegrees(Math.atan2(tz - z, tx - x) - Math.toRadians(90.0));
        double len = Math.abs(hitchBlocks);
        double vertical = ty - y;
        double horizontal = Math.sqrt(Math.max(0.0, len * len - vertical * vertical));
        double signed = Math.copySign(horizontal, hitchBlocks);
        double a = Math.toRadians(-yaw); // rotate (0, signed) about Y
        double nx = signed * Math.sin(a) + tx;
        double nz = signed * Math.cos(a) + tz;
        double hd = Math.sqrt((tx - x) * (tx - x) + (tz - z) * (tz - z));
        float target = (float) Math.toDegrees(Math.atan2(vertical, Math.max(hd, 0.001)));
        target = Math.max(-20.0F, Math.min(20.0F, target));
        return new Pose(nx, nz, yaw, pitchNow + (target - pitchNow) * 0.25F);
    }

    /** The distance between trailer and tow bar past which the hitch snaps. */
    public static double detachDistance(double baseThreshold, double hitchBlocks) {
        return baseThreshold + Math.abs(hitchBlocks);
    }
}
