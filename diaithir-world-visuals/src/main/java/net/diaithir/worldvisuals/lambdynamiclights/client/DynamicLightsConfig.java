package net.diaithir.worldvisuals.lambdynamiclights.client;

import java.util.List;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class DynamicLightsConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue ENABLED;
    public static final ModConfigSpec.BooleanValue HELD_ITEMS;
    public static final ModConfigSpec.BooleanValue DROPPED_ITEMS;
    public static final ModConfigSpec.BooleanValue ENTITIES;
    public static final ModConfigSpec.BooleanValue SELF_ONLY;
    public static final ModConfigSpec.IntValue MAX_LIGHT_LEVEL;
    public static final ModConfigSpec.IntValue UPDATE_RATE_TICKS;
    public static final ModConfigSpec.IntValue MAX_DISTANCE;
    public static final ModConfigSpec.BooleanValue WATER_SENSITIVE;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> ITEM_OVERRIDES;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> ENTITY_OVERRIDES;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> BLACKLIST;
    public static final ModConfigSpec.BooleanValue DEBUG_OVERLAY;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        ENABLED = b.define("enabled", true);
        HELD_ITEMS = b.define("heldItems", true);
        DROPPED_ITEMS = b.define("droppedItems", true);
        ENTITIES = b.define("entities", true);
        SELF_ONLY = b.define("selfOnly", false);
        MAX_LIGHT_LEVEL = b.defineInRange("maxLightLevel", 15, 0, 15);
        UPDATE_RATE_TICKS = b.defineInRange("updateRateTicks", 2, 1, 10);
        MAX_DISTANCE = b.defineInRange("maxDistance", 32, 8, 64);
        WATER_SENSITIVE = b.define("waterSensitive", true);
        ITEM_OVERRIDES = b.defineListAllowEmpty("itemLuminanceOverrides", List.of(), () -> "modid:item=level", o -> o instanceof String);
        ENTITY_OVERRIDES = b.defineListAllowEmpty("entityLuminanceOverrides", List.of(), () -> "modid:entity=level", o -> o instanceof String);
        BLACKLIST = b.defineListAllowEmpty("blacklistEntities", List.of(), () -> "modid:entity", o -> o instanceof String);
        DEBUG_OVERLAY = b.define("debugOverlay", false);
        SPEC = b.build();
    }

    private DynamicLightsConfig() {}
}
