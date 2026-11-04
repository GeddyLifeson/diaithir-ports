// Ported from Leave My Bars Alone (MPL-2.0) https://github.com/Fuzss/leavemybarsalone
// Modifications: Puzzles Lib config replaced by NeoForge ModConfigSpec; same two options.
package net.diaithir.hudoverlays.leavebars.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class LeaveBarsConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue FOOD_LEVEL;
    public static final ModConfigSpec.BooleanValue EXPERIENCE_BAR;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        FOOD_LEVEL = b.comment("Render food bar when riding a mount.").define("foodLevel", true);
        EXPERIENCE_BAR = b.comment("Render experience bar when not jumping with a mount.").define("experienceBar", true);
        SPEC = b.build();
    }

    private LeaveBarsConfig() {}
}
