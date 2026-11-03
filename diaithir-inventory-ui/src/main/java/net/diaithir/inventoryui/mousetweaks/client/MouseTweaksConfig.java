// Ported from Mouse Tweaks (BSD-3-Clause) https://github.com/YaLTeR/MouseTweaks
// Modifications: Properties-file config replaced by ModConfigSpec; enums merged here; added disabledScreens list in place of the upstream GUI-author API.
package net.diaithir.inventoryui.mousetweaks.client;

import java.util.List;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class MouseTweaksConfig {
    public enum SearchOrder { FIRST_TO_LAST, LAST_TO_FIRST }

    public enum ScrollDirection {
        NORMAL, INVERTED, INVENTORY_POSITION_AWARE, INVENTORY_POSITION_AWARE_INVERTED;

        public boolean isInverted() {
            return this == INVERTED || this == INVENTORY_POSITION_AWARE_INVERTED;
        }

        public boolean isPositionAware() {
            return this == INVENTORY_POSITION_AWARE || this == INVENTORY_POSITION_AWARE_INVERTED;
        }
    }

    public enum ScrollScaling {
        PROPORTIONAL, ALWAYS_ONE;

        /** Scales the scroll distance into a number of items to move; the sign is the direction. */
        public double scale(double scrollDelta) {
            return this == PROPORTIONAL ? scrollDelta : Math.signum(scrollDelta);
        }
    }

    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue RMB_TWEAK;
    public static final ModConfigSpec.BooleanValue LMB_TWEAK_WITH_ITEM;
    public static final ModConfigSpec.BooleanValue LMB_TWEAK_WITHOUT_ITEM;
    public static final ModConfigSpec.BooleanValue WHEEL_TWEAK;
    public static final ModConfigSpec.EnumValue<SearchOrder> WHEEL_SEARCH_ORDER;
    public static final ModConfigSpec.EnumValue<ScrollDirection> WHEEL_SCROLL_DIRECTION;
    public static final ModConfigSpec.EnumValue<ScrollScaling> SCROLL_ITEM_SCALING;
    public static final ModConfigSpec.BooleanValue DEBUG;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> DISABLED_SCREENS;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        RMB_TWEAK = b.comment("Drag with the right mouse button while holding an item to place one item into each slot passed over")
                .define("rmbTweak", true);
        LMB_TWEAK_WITH_ITEM = b.comment("Drag with the left mouse button while holding an item to collect matching stacks (shift: move them)")
                .define("lmbTweakWithItem", true);
        LMB_TWEAK_WITHOUT_ITEM = b.comment("Shift + left drag without an item shift-clicks every slot passed over")
                .define("lmbTweakWithoutItem", true);
        WHEEL_TWEAK = b.comment("Scroll the mouse wheel over a slot to move items between inventories")
                .define("wheelTweak", true);
        WHEEL_SEARCH_ORDER = b.comment("Order in which slots are searched when pulling items")
                .defineEnum("wheelSearchOrder", SearchOrder.LAST_TO_FIRST);
        WHEEL_SCROLL_DIRECTION = b.comment("NORMAL: down pushes, up pulls. INVERTED: opposite. POSITION_AWARE variants depend on where the other inventory is")
                .defineEnum("wheelScrollDirection", ScrollDirection.NORMAL);
        SCROLL_ITEM_SCALING = b.comment("PROPORTIONAL: several wheel clicks move several items. ALWAYS_ONE: always one item (macOS compatibility)")
                .defineEnum("scrollItemScaling", ScrollScaling.PROPORTIONAL);
        DEBUG = b.comment("Log debug information").define("debug", false);
        DISABLED_SCREENS = b.comment("Screen class names (simple or full) where Mouse Tweaks is disabled")
                .defineListAllowEmpty("disabledScreens", List.of(), () -> "", o -> o instanceof String);
        SPEC = b.build();
    }

    private MouseTweaksConfig() {}
}
