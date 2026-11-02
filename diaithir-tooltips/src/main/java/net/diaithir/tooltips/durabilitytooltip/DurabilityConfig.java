package net.diaithir.tooltips.durabilitytooltip;

import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

final class DurabilityConfig {
    enum Format { FULL, COMPACT, PERCENT_ONLY }
    enum Position { AFTER_NAME, END }

    static final ModConfigSpec SPEC;
    private static final ModConfigSpec.BooleanValue ENABLED;
    private static final ModConfigSpec.EnumValue<Format> FORMAT;
    private static final ModConfigSpec.BooleanValue SHOW_PERCENT;
    private static final ModConfigSpec.BooleanValue COLOR;
    private static final ModConfigSpec.BooleanValue ONLY_ADVANCED;
    private static final ModConfigSpec.BooleanValue SHOW_UNBREAKABLE;
    private static final ModConfigSpec.BooleanValue HIDE_FULL;
    private static final ModConfigSpec.EnumValue<Position> POSITION;
    private static final ModConfigSpec.IntValue LOW;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        ENABLED = b.comment("Master toggle").define("enabled", true);
        FORMAT = b.comment("Text layout").defineEnum("format", Format.FULL);
        SHOW_PERCENT = b.comment("Append (xx%) in FULL/COMPACT").define("showPercent", true);
        COLOR = b.comment("Colour by remaining durability").define("colorByDurability", true);
        ONLY_ADVANCED = b.comment("Only show with advanced tooltips (F3+H)").define("onlyWhenAdvanced", false);
        SHOW_UNBREAKABLE = b.comment("Show a line for unbreakable items").define("showUnbreakable", true);
        HIDE_FULL = b.comment("Hide on undamaged items").define("hideWhenFull", false);
        POSITION = b.comment("Line placement").defineEnum("position", Position.END);
        LOW = b.comment("Below this percent the line is red").defineInRange("lowThresholdPercent", 15, 1, 50);
        SPEC = b.build();
    }

    static volatile boolean enabled = true;
    static volatile Format format = Format.FULL;
    static volatile boolean showPercent = true;
    static volatile boolean color = true;
    static volatile boolean onlyAdvanced = false;
    static volatile boolean showUnbreakable = true;
    static volatile boolean hideFull = false;
    static volatile Position position = Position.END;
    static volatile int low = 15;

    private DurabilityConfig() {}

    static void onLoad(ModConfigEvent event) {
        if (event.getConfig().getSpec() != SPEC) return;
        enabled = ENABLED.get();
        format = FORMAT.get();
        showPercent = SHOW_PERCENT.get();
        color = COLOR.get();
        onlyAdvanced = ONLY_ADVANCED.get();
        showUnbreakable = SHOW_UNBREAKABLE.get();
        hideFull = HIDE_FULL.get();
        position = POSITION.get();
        low = LOW.get();
    }

    static void setEnabled(boolean value) {
        enabled = value;
        ENABLED.set(value);
        SPEC.save();
    }
}
