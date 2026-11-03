/* SPDX-License-Identifier: MIT */

package li.cil.oc2.client.item;

import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;

import static net.minecraft.core.component.DataComponents.DYED_COLOR;

@SuppressWarnings("unused")
public final class CustomItemColors {
    public static final int BLACK = 0xFF404040;
    public static final int GREY = 0xFF555555;
    public static final int LIGHT_GREY = 0xFF898989;
    public static final int WHITE = 0xFFCACACA;

    public static final int LIME = 0xFF65DA2B;
    public static final int GREEN = 0xFF1C9C31;
    public static final int CYAN = 0xFF11C5BD;
    public static final int BLUE = 0xFF4F66E8;
    public static final int LIGHT_BLUE = 0xFF1192C5;
    public static final int PURPLE = 0xFF8F02CA;
    public static final int MAGENTA = 0xFFC61087;
    public static final int PINK = 0xFFDB51BD;
    public static final int ORANGE = 0xFFDD803D;
    public static final int RED = 0xFFDD3D3D;
    public static final int BROWN = 0xFF745C42;
    public static final int YELLOW = 0xFFFFFC49;

    ///////////////////////////////////////////////////////////////////

    private static final int NO_TINT = 0xFFFFFFFF;

    ///////////////////////////////////////////////////////////////////

    public static void initialize() {
        // TODO(oc2-restore): ItemColors no longer exists in 26.1. Item tinting is now defined by the
        // item model definitions (tint sources, "minecraft:dye" on tint index 1) for the hard drives
        // and floppies, so there is nothing to register here.
    }

    public static int getColorByDye(final DyeColor dye) {
        return switch (dye) {
            case WHITE -> WHITE;
            case ORANGE -> ORANGE;
            case MAGENTA -> MAGENTA;
            case LIGHT_BLUE -> LIGHT_BLUE;
            case YELLOW -> YELLOW;
            case LIME -> LIME;
            case PINK -> PINK;
            case GRAY -> GREY;
            case LIGHT_GRAY -> LIGHT_GREY;
            case CYAN -> CYAN;
            case PURPLE -> PURPLE;
            case BLUE -> BLUE;
            case BROWN -> BROWN;
            case GREEN -> GREEN;
            case RED -> RED;
            case BLACK -> BLACK;
        };
    }

    public static int getColor(final ItemStack stack) {
        return DyedItemColor.getOrDefault(stack, GREY);
    }

    public static ItemStack withColor(final ItemStack stack, final DyeColor color) {
        return withColor(stack, getColorByDye(color));
    }

    public static ItemStack withColor(final ItemStack stack, final int color) {
        stack.applyComponents(
            DataComponentPatch.builder()
            .set(DYED_COLOR, new DyedItemColor(color))
            .build()
        );
        return stack;
    }
}
