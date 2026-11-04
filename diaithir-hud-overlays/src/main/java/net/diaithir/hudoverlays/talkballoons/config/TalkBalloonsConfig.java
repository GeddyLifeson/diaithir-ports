// Ported from Talk Balloons (LGPL-3.0-only) https://github.com/CERBON-MODS/Talk-Balloons
// Modifications: ModConfigSpec replaces Sunset/YACL config; options mirror upstream defaults
package net.diaithir.hudoverlays.talkballoons.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class TalkBalloonsConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue ENABLED;
    public static final ModConfigSpec.DoubleValue HEIGHT_OFFSET;
    public static final ModConfigSpec.IntValue DISTANCE_BETWEEN_BALLOONS;
    public static final ModConfigSpec.IntValue MAX_BALLOONS;
    public static final ModConfigSpec.IntValue MIN_BALLOON_WIDTH;
    public static final ModConfigSpec.IntValue MAX_BALLOON_WIDTH;
    public static final ModConfigSpec.IntValue BALLOON_PADDING;
    public static final ModConfigSpec.IntValue BALLOON_AGE;
    public static final ModConfigSpec.IntValue TEXT_COLOR;
    public static final ModConfigSpec.IntValue BALLOON_TINT;
    public static final ModConfigSpec.DoubleValue BALLOON_OPACITY;
    public static final ModConfigSpec.DoubleValue BALLOON_SNEAKING_OPACITY;
    public static final ModConfigSpec.BooleanValue SHOW_OWN_BALLOON;
    public static final ModConfigSpec.BooleanValue ONLY_DISPLAY_BALLOONS;
    public static final ModConfigSpec.DoubleValue BALLOON_FADE_OUT;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        ENABLED = b.comment("When disabled, all balloons stop rendering and being created").define("isEnabled", true);
        HEIGHT_OFFSET = b.comment("Offset of the balloons above the player's head (blocks)").defineInRange("balloonsHeightOffset", 0.4D, -16.0D, 16.0D);
        DISTANCE_BETWEEN_BALLOONS = b.comment("Pixel distance between stacked balloons").defineInRange("distanceBetweenBalloons", 3, 0, 20);
        MAX_BALLOONS = b.comment("Maximum number of balloons shown per player").defineInRange("maxBalloons", 7, 1, 16);
        MIN_BALLOON_WIDTH = b.comment("Minimum balloon width in pixels").defineInRange("minBalloonWidth", 8, 8, 512);
        MAX_BALLOON_WIDTH = b.comment("Maximum balloon width in pixels").defineInRange("maxBalloonWidth", 180, 8, 512);
        BALLOON_PADDING = b.comment("Padding around balloon text in pixels").defineInRange("balloonPadding", 2, 0, 64);
        BALLOON_AGE = b.comment("Seconds a balloon is displayed; 0 = always").defineInRange("balloonAge", 15, 0, 120);
        TEXT_COLOR = b.comment("Text colour as RGB integer (0x141414 = 1315860)").defineInRange("textColor", 0x141414, 0, 0xFFFFFF);
        BALLOON_TINT = b.comment("Balloon tint as RGB integer (0xF1F6F8 = 15857400)").defineInRange("balloonTint", 0xF1F6F8, 0, 0xFFFFFF);
        BALLOON_OPACITY = b.comment("Balloon opacity").defineInRange("balloonOpacity", 0.95D, 0.15D, 1.0D);
        BALLOON_SNEAKING_OPACITY = b.comment("Balloon opacity while the player is sneaking").defineInRange("balloonSneakingOpacity", 0.65D, 0.15D, 1.0D);
        SHOW_OWN_BALLOON = b.comment("Show your own balloon in third person").define("showOwnBalloon", true);
        ONLY_DISPLAY_BALLOONS = b.comment("Do not show chat messages in the chat window, only balloons").define("onlyDisplayBalloons", false);
        BALLOON_FADE_OUT = b.comment("Seconds the fade-out takes at the end of a balloon's life").defineInRange("balloonFadeOut", 0.4D, 0.0D, 5.0D);
        SPEC = b.build();
    }

    private TalkBalloonsConfig() {}
}
