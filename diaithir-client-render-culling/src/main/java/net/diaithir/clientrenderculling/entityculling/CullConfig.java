package net.diaithir.clientrenderculling.entityculling;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

final class CullConfig {
    static final ModConfigSpec SPEC;
    static final ModConfigSpec.BooleanValue ENABLED, CULL_ENTITIES, CULL_BLOCK_ENTITIES, CULL_PLAYERS, CULL_INVIS_GLOW, DEBUG;
    static final ModConfigSpec.IntValue MAX_DIST, ALWAYS_DIST, RECHECK_MS, SAMPLES, THREADS, MAX_TARGETS;
    static final ModConfigSpec.ConfigValue<List<? extends String>> ENTITY_WL, BE_WL;

    static volatile Set<String> entityWhitelist = Set.of();
    static volatile Set<String> blockEntityWhitelist = Set.of();

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        ENABLED = b.define("enabled", true);
        CULL_ENTITIES = b.define("cullEntities", true);
        CULL_BLOCK_ENTITIES = b.define("cullBlockEntities", true);
        MAX_DIST = b.defineInRange("maxDistance", 128, 16, 256);
        ALWAYS_DIST = b.defineInRange("alwaysRenderDistance", 8, 0, 32);
        RECHECK_MS = b.defineInRange("recheckIntervalMs", 100, 25, 1000);
        SAMPLES = b.defineInRange("samplePoints", 5, 1, 9);
        THREADS = b.defineInRange("workerThreads", 1, 1, 4);
        MAX_TARGETS = b.defineInRange("maxTargetsPerTick", 1024, 64, 4096);
        ENTITY_WL = b.defineListAllowEmpty("entityWhitelist", List.of("minecraft:ender_dragon", "minecraft:wither", "minecraft:end_crystal"), () -> "minecraft:pig", o -> o instanceof String);
        BE_WL = b.defineListAllowEmpty("blockEntityWhitelist", List.of("minecraft:beacon", "minecraft:end_gateway", "minecraft:end_portal"), () -> "minecraft:chest", o -> o instanceof String);
        CULL_PLAYERS = b.define("cullPlayers", false);
        CULL_INVIS_GLOW = b.define("cullInvisibleOrGlowing", false);
        DEBUG = b.define("debugOverlay", false);
        SPEC = b.build();
    }

    private CullConfig() {}

    static void onReload(ModConfigEvent event) {
        if (event.getConfig().getSpec() != SPEC) return;
        entityWhitelist = new HashSet<>(ENTITY_WL.get());
        blockEntityWhitelist = new HashSet<>(BE_WL.get());
        CullEngine.resize();
    }
}
