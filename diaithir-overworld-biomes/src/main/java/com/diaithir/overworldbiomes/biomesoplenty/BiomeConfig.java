package com.diaithir.overworldbiomes.biomesoplenty;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class BiomeConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.DoubleValue WOODLAND_WEIGHT;
    public static final ModConfigSpec.DoubleValue WET_WEIGHT;
    public static final ModConfigSpec.DoubleValue DRY_WEIGHT;
    public static final ModConfigSpec.DoubleValue COLD_WEIGHT;
    public static final ModConfigSpec.DoubleValue OPEN_WEIGHT;
    public static final ModConfigSpec.BooleanValue GENERATE_TREES;
    public static final ModConfigSpec.BooleanValue GENERATE_FLOWERS;
    public static final ModConfigSpec.BooleanValue SPAWN_VANILLA_MOBS;
    public static final ModConfigSpec.ConfigValue<String> COMPAT_MODE;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        b.push("weights");
        WOODLAND_WEIGHT = b.defineInRange("woodland", 1.0, 0.0, 3.0);
        WET_WEIGHT = b.defineInRange("wet", 1.0, 0.0, 3.0);
        DRY_WEIGHT = b.defineInRange("dry", 1.0, 0.0, 3.0);
        COLD_WEIGHT = b.defineInRange("cold", 1.0, 0.0, 3.0);
        OPEN_WEIGHT = b.defineInRange("open", 1.0, 0.0, 3.0);
        b.pop();
        GENERATE_TREES = b.define("generateTrees", true);
        GENERATE_FLOWERS = b.define("generateFlowers", true);
        SPAWN_VANILLA_MOBS = b.define("spawnVanillaMobsInBiomes", true);
        COMPAT_MODE = b.define("compatMode", "loose");
        SPEC = b.build();
    }

    private BiomeConfig() {}
}
