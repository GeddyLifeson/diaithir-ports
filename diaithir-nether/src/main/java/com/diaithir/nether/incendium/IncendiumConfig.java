package com.diaithir.nether.incendium;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class IncendiumConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.DoubleValue BIOME_WEIGHT_MULTIPLIER;
    public static final ModConfigSpec.DoubleValue STRUCTURE_SPACING_SCALE;
    public static final ModConfigSpec.BooleanValue ENABLE_CUSTOM_MOBS;
    public static final ModConfigSpec.DoubleValue MOB_HEALTH_MULTIPLIER;
    public static final ModConfigSpec.BooleanValue UNIQUE_LOOT_ONLY_IN_STRUCTURES;
    public static final ModConfigSpec.DoubleValue SPAWN_CAP_MODIFIER;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        BIOME_WEIGHT_MULTIPLIER = b.defineInRange("biome_weight_multiplier", 1.0, 0.0, 2.0);
        STRUCTURE_SPACING_SCALE = b.defineInRange("structure_spacing_scale", 1.0, 0.25, 4.0);
        ENABLE_CUSTOM_MOBS = b.define("enable_custom_mobs", true);
        MOB_HEALTH_MULTIPLIER = b.defineInRange("mob_health_multiplier", 1.0, 0.1, 10.0);
        UNIQUE_LOOT_ONLY_IN_STRUCTURES = b.define("enable_unique_loot_only_in_structures", true);
        SPAWN_CAP_MODIFIER = b.defineInRange("spawn_cap_modifier", 1.0, 0.0, 4.0);
        SPEC = b.build();
    }

    private IncendiumConfig() {}
}
