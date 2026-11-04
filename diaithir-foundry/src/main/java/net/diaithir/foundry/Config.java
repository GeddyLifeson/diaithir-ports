package net.diaithir.foundry;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class Config {
    public static final ModConfigSpec.Builder B = new ModConfigSpec.Builder();
    public static final ModConfigSpec.IntValue TANK_BUCKETS = B.comment("Molten metal capacity in buckets").defineInRange("tankBuckets", 16, 1, 1024);
    public static final ModConfigSpec.IntValue MELT_UNITS_PER_RUN = B.comment("Units of molten metal produced per melt step").defineInRange("meltUnitsPerRun", 90, 1, 1000);
    public static final ModConfigSpec.IntValue TICKS_PER_RUN = B.comment("Ticks between foundry steps").defineInRange("ticksPerRun", 10, 1, 200);
    public static final ModConfigSpec SPEC = B.build();

    private Config() {
    }
}
