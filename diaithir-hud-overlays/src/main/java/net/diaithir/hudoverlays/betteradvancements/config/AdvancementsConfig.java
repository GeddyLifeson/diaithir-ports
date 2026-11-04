package net.diaithir.hudoverlays.betteradvancements.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class AdvancementsConfig {
    public enum CompletionFilter { ALL, HIDE_COMPLETED, ONLY_INCOMPLETE }

    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue SEARCH_ENABLED;
    public static final ModConfigSpec.BooleanValue MATCH_DESCRIPTION;
    public static final ModConfigSpec.DoubleValue DIM_OPACITY;
    public static final ModConfigSpec.BooleanValue COMPACT_TABS;
    public static final ModConfigSpec.IntValue TOOLTIP_MAX_WIDTH;
    public static final ModConfigSpec.IntValue BACKGROUND_TINT;
    public static final ModConfigSpec.BooleanValue REMEMBER_LAST;
    public static final ModConfigSpec.BooleanValue SCROLL_SWITCH;
    public static final ModConfigSpec.BooleanValue NUMBER_KEYS;
    public static final ModConfigSpec.BooleanValue SHOW_PROGRESS;
    public static final ModConfigSpec.EnumValue<CompletionFilter> COMPLETION_FILTER;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        b.push("search");
        SEARCH_ENABLED = b.define("enabled", true);
        MATCH_DESCRIPTION = b.define("matchDescription", true);
        DIM_OPACITY = b.defineInRange("dimOpacity", 0.35, 0.1, 0.9);
        b.pop();
        b.push("layout");
        COMPACT_TABS = b.define("compactTabs", false);
        TOOLTIP_MAX_WIDTH = b.defineInRange("tooltipMaxWidth", 220, 120, 400);
        BACKGROUND_TINT = b.defineInRange("backgroundTint", 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
        b.pop();
        b.push("tabs");
        REMEMBER_LAST = b.define("rememberLast", true);
        SCROLL_SWITCH = b.define("scrollSwitch", true);
        NUMBER_KEYS = b.define("numberKeys", true);
        b.pop();
        b.push("display");
        SHOW_PROGRESS = b.define("showProgress", true);
        COMPLETION_FILTER = b.defineEnum("completionFilter", CompletionFilter.ALL);
        b.pop();
        SPEC = b.build();
    }

    private AdvancementsConfig() {}
}
