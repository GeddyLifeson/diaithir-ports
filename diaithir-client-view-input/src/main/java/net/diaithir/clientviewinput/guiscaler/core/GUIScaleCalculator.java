// Ported from GUI Scaler (LGPL-3.0-only) https://github.com/zendiik/gui-scaler
// Modifications: moved to the guiscaler feature package; logic unchanged.
package net.diaithir.clientviewinput.guiscaler.core;

import java.util.Map;

public final class GUIScaleCalculator {
    private GUIScaleCalculator() {}

    public static int calculateOptimalScale(int width, int height, ScaleMode mode, Map<Integer, Integer> customRules) {
        if (mode == ScaleMode.CUSTOM) {
            return calculateCustomScale(width, customRules);
        }
        return calculateAutoScale(width, height);
    }

    private static int calculateAutoScale(int width, int height) {
        long totalPixels = (long) width * height;

        // HiDPI/4K territory (2560+ width, or 4.5M+ pixels)
        if (width >= 2560 || totalPixels > 4_500_000L) {
            return 3;
        }
        // HD and above (1280+ width, or 2M+ pixels)
        if (width >= 1280 || totalPixels > 2_000_000L) {
            return 2;
        }
        // Let Minecraft decide
        return 0;
    }

    private static int calculateCustomScale(int width, Map<Integer, Integer> rules) {
        return rules.entrySet().stream()
                .filter(e -> width >= e.getKey())
                .max(Map.Entry.comparingByKey())
                .map(Map.Entry::getValue)
                .orElse(0);
    }
}
