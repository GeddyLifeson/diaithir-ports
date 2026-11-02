package com.diaithir.end.nullscape;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class NullscapeConfig {
 public static final ModConfigSpec SPEC;
 public static final ModConfigSpec.DoubleValue TERRAIN_SCALE;
 public static final ModConfigSpec.DoubleValue VOID_GAP_THRESHOLD;
 public static final ModConfigSpec.DoubleValue SPIRE_HEIGHT_MULTIPLIER;
 public static final ModConfigSpec.DoubleValue CHORUS_DENSITY_MULTIPLIER;
 public static final ModConfigSpec.BooleanValue ENABLE_NEW_BIOMES;
 public static final ModConfigSpec.IntValue CENTRAL_ISLAND_BLEND_RADIUS;
 public static final ModConfigSpec.DoubleValue MOB_SPAWN_MULTIPLIER;

 static {
 ModConfigSpec.Builder b = new ModConfigSpec.Builder();
 b.push("terrain");
 TERRAIN_SCALE = b.comment("Horizontal terrain scale multiplier").defineInRange("terrain_scale", 1.0, 0.25, 4.0);
 VOID_GAP_THRESHOLD = b.comment("Continent mask threshold; higher = more void").defineInRange("void_gap_threshold", 0.2, -1.0, 1.0);
 SPIRE_HEIGHT_MULTIPLIER = b.defineInRange("spire_height_multiplier", 1.0, 0.1, 4.0);
 CENTRAL_ISLAND_BLEND_RADIUS = b.comment("Blocks beyond ~1000 over which terrain blends").defineInRange("central_island_blend_radius", 400, 0, 4096);
 b.pop();
 b.push("life");
 CHORUS_DENSITY_MULTIPLIER = b.defineInRange("chorus_density_multiplier", 0.2, 0.0, 1.0);
 MOB_SPAWN_MULTIPLIER = b.defineInRange("mob_spawn_multiplier", 0.6, 0.0, 2.0);
 b.pop();
 b.push("biomes");
 ENABLE_NEW_BIOMES = b.comment("WARNING: requires world restart and only affects ungenerated chunks").define("enable_new_biomes", true);
 b.pop();
 SPEC = b.build();
 }

 private NullscapeConfig() {}
}
