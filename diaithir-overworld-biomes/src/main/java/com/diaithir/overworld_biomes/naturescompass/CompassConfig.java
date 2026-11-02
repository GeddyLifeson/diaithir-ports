package com.diaithir.overworld_biomes.naturescompass;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public final class CompassConfig {
    public enum ReadoutPosition { ACTIONBAR, TOP_LEFT, TOP_RIGHT }
    public enum SortMode { ALPHABETICAL, SOURCE_MOD }

    public static final ModConfigSpec SERVER_SPEC;
    public static final ModConfigSpec CLIENT_SPEC;

    public static final ModConfigSpec.IntValue MAX_RADIUS;
    public static final ModConfigSpec.DoubleValue BUDGET_MS;
    public static final ModConfigSpec.IntValue STEP_COARSE;
    public static final ModConfigSpec.IntValue STEP_FINE;
    public static final ModConfigSpec.BooleanValue ALLOW_OTHER_DIMENSIONS;
    public static final ModConfigSpec.IntValue COOLDOWN_TICKS;
    public static final ModConfigSpec.BooleanValue DURABILITY_USE;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> BLACKLIST;

    public static final ModConfigSpec.BooleanValue SHOW_READOUT;
    public static final ModConfigSpec.EnumValue<ReadoutPosition> READOUT_POSITION;
    public static final ModConfigSpec.EnumValue<SortMode> GUI_SORT;
    public static final ModConfigSpec.BooleanValue REMEMBER_FILTER;

    static {
        ModConfigSpec.Builder s = new ModConfigSpec.Builder();
        MAX_RADIUS = s.defineInRange("maxSearchRadius", 6400, 512, 30000);
        BUDGET_MS = s.defineInRange("searchBudgetMsPerTick", 2.0, 0.1, 20.0);
        STEP_COARSE = s.defineInRange("searchStepCoarse", 32, 8, 256);
        STEP_FINE = s.defineInRange("searchStepFine", 4, 4, 32);
        ALLOW_OTHER_DIMENSIONS = s.define("allowOtherDimensions", true);
        COOLDOWN_TICKS = s.defineInRange("cooldownTicks", 40, 0, 6000);
        DURABILITY_USE = s.define("durabilityUse", false);
        BLACKLIST = s.defineListAllowEmpty("blacklistedBiomes", List.of(), () -> "", o -> o instanceof String);
        SERVER_SPEC = s.build();

        ModConfigSpec.Builder c = new ModConfigSpec.Builder();
        SHOW_READOUT = c.define("showDistanceReadout", true);
        READOUT_POSITION = c.defineEnum("readoutPosition", ReadoutPosition.ACTIONBAR);
        GUI_SORT = c.defineEnum("guiSortMode", SortMode.ALPHABETICAL);
        REMEMBER_FILTER = c.define("rememberLastFilter", true);
        CLIENT_SPEC = c.build();
    }

    private CompassConfig() {}
}
