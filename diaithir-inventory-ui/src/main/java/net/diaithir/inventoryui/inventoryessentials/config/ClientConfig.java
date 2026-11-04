package net.diaithir.inventoryui.inventoryessentials.config;

import java.util.List;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class ClientConfig {
    public enum Anchor { TOP_RIGHT, TOP_LEFT, BOTTOM_RIGHT, BOTTOM_LEFT }

    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue ENABLE_SORT_BUTTON;
    public static final ModConfigSpec.EnumValue<Anchor> BUTTON_ANCHOR;
    public static final ModConfigSpec.BooleanValue DRAG_MOVE_ENABLED;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> SORT_CATEGORY_ORDER;
    public static final ModConfigSpec.BooleanValue SHIFT_SCROLL_MOVE;
    public static final ModConfigSpec.BooleanValue LOCKED_SLOT_OVERLAY;
    public static final ModConfigSpec.BooleanValue SHOW_TOOLTIP_HINTS;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        ENABLE_SORT_BUTTON = b.define("enableSortButton", true);
        BUTTON_ANCHOR = b.defineEnum("buttonAnchor", Anchor.TOP_RIGHT);
        DRAG_MOVE_ENABLED = b.define("dragMoveEnabled", true);
        SORT_CATEGORY_ORDER = b.defineListAllowEmpty("sortCategoryOrder",
                List.of("tools", "weapons", "armor", "blocks", "food", "other"), () -> "", o -> o instanceof String);
        SHIFT_SCROLL_MOVE = b.define("shiftScrollMove", true);
        LOCKED_SLOT_OVERLAY = b.define("lockedSlotOverlay", true);
        SHOW_TOOLTIP_HINTS = b.define("showTooltipHints", true);
        SPEC = b.build();
    }

    private ClientConfig() {}
}
