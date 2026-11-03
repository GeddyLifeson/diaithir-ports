package net.diaithir.inventoryui.trashslot.client;

import java.util.List;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class TrashClientConfig {
    public enum Anchor { BOTTOM_RIGHT, BOTTOM_LEFT, TOP_RIGHT, TOP_LEFT }

    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue ENABLED;
    public static final ModConfigSpec.EnumValue<Anchor> ANCHOR;
    public static final ModConfigSpec.IntValue OFFSET_X;
    public static final ModConfigSpec.IntValue OFFSET_Y;
    public static final ModConfigSpec.BooleanValue REMEMBER_POSITION;
    public static final ModConfigSpec.IntValue POS_X;
    public static final ModConfigSpec.IntValue POS_Y;
    public static final ModConfigSpec.BooleanValue HAS_POS;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> ALLOWED_SCREENS;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> BLOCKED_SCREENS;
    public static final ModConfigSpec.BooleanValue UNDO_ENABLED;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> CONFIRM_FOR_TAGS;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> PROTECTED_ITEMS;
    public static final ModConfigSpec.IntValue CONFIRM_THRESHOLD;
    public static final ModConfigSpec.BooleanValue SHOW_IN_CREATIVE;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        ENABLED = b.comment("Trash slot visible (persisted toggle state)").define("enabledByDefault", true);
        ANCHOR = b.defineEnum("defaultAnchor", Anchor.BOTTOM_RIGHT);
        OFFSET_X = b.defineInRange("defaultOffsetX", 4, -4096, 4096);
        OFFSET_Y = b.defineInRange("defaultOffsetY", 4, -4096, 4096);
        REMEMBER_POSITION = b.define("rememberPosition", true);
        POS_X = b.defineInRange("posX", 0, -100000, 100000);
        POS_Y = b.defineInRange("posY", 0, -100000, 100000);
        HAS_POS = b.define("hasSavedPosition", false);
        ALLOWED_SCREENS = b.comment("Screen class names (simple or full); empty = all").defineListAllowEmpty("allowedScreens", List.of(), () -> "", o -> o instanceof String);
        BLOCKED_SCREENS = b.defineListAllowEmpty("blockedScreens", List.of(), () -> "", o -> o instanceof String);
        UNDO_ENABLED = b.define("undoEnabled", true);
        CONFIRM_FOR_TAGS = b.comment("Item tags requiring confirmation, e.g. c:ingots/netherite").defineListAllowEmpty("confirmForTags", List.of(), () -> "", o -> o instanceof String);
        PROTECTED_ITEMS = b.comment("Item ids requiring confirmation").defineListAllowEmpty("protectedItems", List.of(), () -> "", o -> o instanceof String);
        CONFIRM_THRESHOLD = b.comment("Confirm when trashing more than N items; 0 = off").defineInRange("confirmThreshold", 0, 0, 100000);
        SHOW_IN_CREATIVE = b.define("showInCreative", true);
        SPEC = b.build();
    }

    private TrashClientConfig() {}
}
