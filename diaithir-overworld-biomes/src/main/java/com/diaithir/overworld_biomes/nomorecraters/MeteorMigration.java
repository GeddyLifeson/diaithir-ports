package com.diaithir.overworld_biomes.nomorecraters;

import java.util.Map;

/** Legacy id/config migration table (original is a pure mixin mod; only config keys are preserved). */
public final class MeteorMigration {
    public static final String LEGACY_MOD_ID = "nomorecraters";
    public static final String LEGACY_CONFIG_FILE = "nomorecraters-common.toml";
    /** legacy config key -> current key (identity-mapped; kept for explicit data migration). */
    public static final Map<String, String> CONFIG_KEYS = Map.of(
            "enabled", "enabled",
            "burialDepth", "burialDepth",
            "removeCraterBlocks", "removeCraterBlocks",
            "structureProtection", "structureProtection",
            "protectedStructureTags", "protectedStructureTags",
            "surfaceHint", "surfaceHint",
            "logSuppressions", "logSuppressions");

    private MeteorMigration() {}
}
