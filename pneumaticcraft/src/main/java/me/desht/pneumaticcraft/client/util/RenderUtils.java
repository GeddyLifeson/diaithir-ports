/*
 * Stub: the client side of this class has not been ported to 26.1.2 yet (see PORT_NOTES.md).
 * The original source is kept under port-stash/.
 */
package me.desht.pneumaticcraft.client.util;


public final class RenderUtils {
    private RenderUtils() {}
    public static int[] decomposeColor(int color) {
        return new int[]{color >> 24 & 0xff, color >> 16 & 0xff, color >> 8 & 0xff, color & 0xff};
    }

    public static float[] decomposeColorF(int color) {
        float[] res = new float[4];
        res[0] = (color >> 24 & 0xff) / 255f;
        res[1] = (color >> 16 & 0xff) / 255f;
        res[2] = (color >> 8  & 0xff) / 255f;
        res[3] = (color       & 0xff) / 255f;
        return res;
    }
}
