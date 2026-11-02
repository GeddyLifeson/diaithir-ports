package com.diaithir.overworld_biomes.nomorecraters;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public final class MeteorConfig {
    public enum Protection { RELOCATE, CANCEL, OFF }

    private static final ModConfigSpec.Builder B = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue ENABLED = B.comment("Enable meteor crater suppression").translation("diaithir_overworld_biomes.config.enabled").define("enabled", true);
    public static final ModConfigSpec.IntValue BURIAL_DEPTH = B.comment("Blocks between surface and top of meteor body").translation("diaithir_overworld_biomes.config.burialDepth").defineInRange("burialDepth", 8, 0, 32);
    public static final ModConfigSpec.BooleanValue REMOVE_CRATER_BLOCKS = B.comment("Skip crater excavation and fall-out scatter").translation("diaithir_overworld_biomes.config.removeCraterBlocks").define("removeCraterBlocks", true);
    public static final ModConfigSpec.EnumValue<Protection> STRUCTURE_PROTECTION = B.comment("Behaviour when footprint overlaps a protected structure").translation("diaithir_overworld_biomes.config.structureProtection").defineEnum("structureProtection", Protection.RELOCATE);
    public static final ModConfigSpec.ConfigValue<List<? extends String>> PROTECTED_STRUCTURE_TAGS = B.comment("Structure ids or #tags that protect terrain").translation("diaithir_overworld_biomes.config.protectedStructureTags").defineListAllowEmpty("protectedStructureTags", List.of("minecraft:village", "minecraft:ancient_city", "minecraft:stronghold"), () -> "minecraft:village", o -> o instanceof String);
    public static final ModConfigSpec.BooleanValue SURFACE_HINT = B.comment("Place a meteor_trace marker above the buried core").translation("diaithir_overworld_biomes.config.surfaceHint").define("surfaceHint", false);
    public static final ModConfigSpec.BooleanValue LOG_SUPPRESSIONS = B.comment("Log each modified placement").translation("diaithir_overworld_biomes.config.logSuppressions").define("logSuppressions", false);

    public static final ModConfigSpec SPEC = B.build();

    private MeteorConfig() {}
}
