package net.diaithir.activity;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public final class ActivityConfig {
    /** Server rules (config/diaithiractivity-common.toml). */
    public static final ModConfigSpec COMMON;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> DISABLED;
    public static final ModConfigSpec.IntValue AFK_SECONDS;
    public static final ModConfigSpec.IntValue UPDATE_TICKS;

    /** Per player look (config/diaithiractivity-client.toml). */
    public static final ModConfigSpec CLIENT;
    public static final ModConfigSpec.BooleanValue SHOW;
    public static final ModConfigSpec.IntValue RANGE;
    public static final ModConfigSpec.DoubleValue ICON_SCALE;
    public static final ModConfigSpec.BooleanValue SHOW_LABEL;
    public static final ModConfigSpec.ConfigValue<String> LABEL_COLOR;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        DISABLED = b.comment("Activity ids that are never shown, for example [\"afk\", \"menu\"]. Ids come from data/diaithiractivity/activity.json.")
            .defineListAllowEmpty("disabled", List.of(), () -> "", o -> o instanceof String);
        AFK_SECONDS = b.comment("Seconds without moving, turning or acting before a player shows as away.").defineInRange("afkSeconds", 120, 10, 36000);
        UPDATE_TICKS = b.comment("How often the server re-checks every player, in ticks.").defineInRange("updateTicks", 5, 1, 100);
        COMMON = b.build();

        ModConfigSpec.Builder c = new ModConfigSpec.Builder();
        SHOW = c.comment("Draw other players' activity above their nameplate.").define("show", true);
        RANGE = c.comment("Furthest distance, in blocks, at which an activity is drawn.").defineInRange("range", 24, 2, 128);
        ICON_SCALE = c.comment("Size of the icon line. 1.0 is the size of the nameplate text.").defineInRange("iconScale", 1.0, 0.5, 3.0);
        SHOW_LABEL = c.comment("Write the name of the activity next to its icon.").define("showLabel", true);
        LABEL_COLOR = c.comment("Colour of the label as a hex value.").define("labelColor", "#DEAA46");
        CLIENT = c.build();
    }

    private ActivityConfig() {}
}
