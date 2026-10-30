package net.diaithir.gear;

import net.neoforged.neoforge.common.ModConfigSpec;

/** Common config (diaithirgear-common.toml). */
public final class GearConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue PARITY_ON, SETS_ON, IGNORE_AGE;
    public static final ModConfigSpec.DoubleValue PARITY_STRENGTH;
    public static final ModConfigSpec.IntValue DEFAULT_AGE;
    public static final ModConfigSpec.ConfigValue<String> AGE_OBJECTIVE;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        b.push("parity");
        PARITY_ON = b.comment("Scale Point Blank bullet hits toward Musket Mod parity.").define("enabled", true);
        PARITY_STRENGTH = b.comment("0 = Point Blank's own numbers, 1 = full parity table.").defineInRange("strength", 1.0, 0.0, 1.0);
        b.pop().push("sets");
        SETS_ON = b.comment("Armour and jewellery set bonuses.").define("enabled", true);
        IGNORE_AGE = b.comment("Apply set bonuses whatever the player's Age.").define("ignoreAge", false);
        b.pop().push("age");
        AGE_OBJECTIVE = b.comment("Scoreboard objective holding the player's Age (1 to 9).").define("objective", "age");
        DEFAULT_AGE = b.comment("Age used when the objective is missing or unset.").defineInRange("defaultAge", 1, 1, 9);
        b.pop();
        SPEC = b.build();
    }

    private GearConfig() {}
}
