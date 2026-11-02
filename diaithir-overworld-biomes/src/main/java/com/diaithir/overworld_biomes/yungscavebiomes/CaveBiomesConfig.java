package com.diaithir.overworld_biomes.yungscavebiomes;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.HashMap;
import java.util.Map;

public final class CaveBiomesConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.DoubleValue RARITY;
    public static final ModConfigSpec.BooleanValue SET_PIECES;
    public static final ModConfigSpec.IntValue SET_PIECE_FREQ;
    public static final ModConfigSpec.BooleanValue AMBIENT_MOBS;
    public static final ModConfigSpec.BooleanValue PARTICLES;
    public static final ModConfigSpec.IntValue MIN_Y;
    public static final ModConfigSpec.IntValue MAX_Y;
    public static final Map<String, ModConfigSpec.BooleanValue> ENABLED = new HashMap<>();

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        RARITY = b.defineInRange("biomeRarityMultiplier", 1.0, 0.0, 2.0);
        for (String n : CaveBiomes.NAMES) ENABLED.put(n, b.define("enable_" + n, true));
        SET_PIECES = b.define("generateSetPieces", true);
        SET_PIECE_FREQ = b.defineInRange("setPieceFrequency", 20, 0, 100);
        AMBIENT_MOBS = b.define("enableAmbientMobs", true);
        PARTICLES = b.define("ambientParticles", true);
        MIN_Y = b.defineInRange("minY", -64, -64, 320);
        MAX_Y = b.defineInRange("maxY", 50, -64, 320);
        SPEC = b.build();
    }

    private CaveBiomesConfig() {}

    public static boolean enabled(String biome) {
        ModConfigSpec.BooleanValue v = ENABLED.get(biome);
        return v != null && v.get();
    }
}
