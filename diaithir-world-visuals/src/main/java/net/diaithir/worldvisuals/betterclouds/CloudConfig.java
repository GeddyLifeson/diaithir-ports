package net.diaithir.worldvisuals.betterclouds;

import net.neoforged.neoforge.common.ModConfigSpec;

/** Clean-room cloud tuning. Defaults give thicker, slightly higher, farther clouds than vanilla. */
public final class CloudConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue ENABLED;
    public static final ModConfigSpec.DoubleValue HEIGHT_OFFSET;
    public static final ModConfigSpec.DoubleValue THICKNESS;
    public static final ModConfigSpec.DoubleValue SPEED;
    public static final ModConfigSpec.IntValue MIN_DISTANCE_CHUNKS;
    public static final ModConfigSpec.DoubleValue OPACITY;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        b.push("clouds");
        ENABLED = b.comment("Master switch. Off means vanilla clouds.").define("enabled", true);
        HEIGHT_OFFSET = b.comment("Blocks added to the dimension's cloud height.").defineInRange("heightOffset", 8.0, -64.0, 256.0);
        THICKNESS = b.comment("Cloud layer thickness in blocks (vanilla 4).").defineInRange("thickness", 8.0, 1.0, 32.0);
        SPEED = b.comment("Wind speed multiplier (vanilla 1).").defineInRange("speed", 1.0, 0.0, 8.0);
        MIN_DISTANCE_CHUNKS = b.comment("Clouds are drawn at least this far, even with a low render distance. 0 keeps vanilla.")
                .defineInRange("minDistanceChunks", 24, 0, 64);
        OPACITY = b.comment("Multiplier on cloud alpha (1 is vanilla).").defineInRange("opacity", 1.0, 0.1, 1.0);
        b.pop();
        SPEC = b.build();
    }

    private CloudConfig() {}

    public static boolean on() {
        return SPEC.isLoaded() && ENABLED.get();
    }
}
