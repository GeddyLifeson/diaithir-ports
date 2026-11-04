package com.diaithir.overworld_biomes.blooming;

import java.util.List;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class BloomingConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue VEGETATION;
    public static final ModConfigSpec.BooleanValue TERRAIN;
    public static final ModConfigSpec.BooleanValue TREES;
    public static final ModConfigSpec.DoubleValue DENSITY;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> DISABLED_BIOMES;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        VEGETATION = b.define("enableVegetationOverhaul", true);
        TERRAIN = b.define("enableTerrainDetail", true);
        TREES = b.define("enableTreeVariants", true);
        DENSITY = b.defineInRange("floraDensityMultiplier", 1.0, 0.25, 3.0);
        DISABLED_BIOMES = b.defineListAllowEmpty("disabledBiomes", List.of(), () -> "minecraft:plains", o -> o instanceof String);
        SPEC = b.build();
    }

    private BloomingConfig() {}

    public static boolean test(String key) {
        return switch (key) {
            case "vegetation" -> VEGETATION.get();
            case "terrain" -> TERRAIN.get();
            case "trees" -> TREES.get();
            case "density_low" -> DENSITY.get() < 0.75;
            case "density_normal" -> DENSITY.get() >= 0.75 && DENSITY.get() <= 1.5;
            case "density_high" -> DENSITY.get() > 1.5;
            default -> false;
        };
    }
}
