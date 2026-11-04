// Ported from AutoHUD (LGPL-3.0-only) https://github.com/Crendgrim/AutoHUD
// Modifications: Replaces DrawContextMixin/RenderSystem shader color alpha injection; applies a global GUI alpha multiplier through GuiGraphicsExtractor.
package net.diaithir.hudoverlays.autohud.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class HudAlpha {
    private static float alpha = 1f;
    private static boolean active;

    private HudAlpha() {}

    public static float alpha() { return alpha; }
    public static boolean active() { return active; }

    public static void begin(float a) {
        alpha = Math.max(0f, Math.min(1f, a));
        active = alpha < 1f;
    }

    public static void end(GuiGraphicsExtractor g) {
        alpha = 1f;
        active = false;
    }
}
