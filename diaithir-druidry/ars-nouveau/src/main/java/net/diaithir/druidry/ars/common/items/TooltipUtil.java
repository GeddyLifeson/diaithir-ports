package net.diaithir.druidry.ars.common.items;

import net.minecraft.client.Minecraft;

/** Port helper: TooltipFlag no longer exposes shift state in 26.1; tooltips are only built client side. */
public final class TooltipUtil {
    private TooltipUtil() {}

    public static boolean hasShiftDown() {
        try {
            return Minecraft.getInstance().hasShiftDown();
        } catch (Throwable t) {
            return false;
        }
    }
}
