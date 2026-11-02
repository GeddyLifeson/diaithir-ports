package com.diaithir.nether.netherex;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class NetherExConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.IntValue W_ASHEN, W_SLAG, W_RUST, W_BRIMSTONE, W_MARSH, W_FROST;
    public static final ModConfigSpec.BooleanValue REPLACE_VANILLA, STRUCTURES_IN_NEW, ALIASES, FIRE_IMMUNE_GEAR, DISABLE_NOISE_WITH_TERRAIN_MODS;
    public static final ModConfigSpec.DoubleValue TERRAIN_SCALE, SPAWN_MULT;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        b.push("biomes");
        W_ASHEN = b.defineInRange("ashenBarrensWeight", 10, 0, 100);
        W_SLAG = b.defineInRange("slagfieldWeight", 10, 0, 100);
        W_RUST = b.defineInRange("rustCanopyWeight", 8, 0, 100);
        W_BRIMSTONE = b.defineInRange("brimstoneHollowsWeight", 8, 0, 100);
        W_MARSH = b.defineInRange("cinderMarshWeight", 8, 0, 100);
        W_FROST = b.defineInRange("frostfireRiftWeight", 2, 0, 100);
        REPLACE_VANILLA = b.define("replaceVanillaBiomes", false);
        b.pop();
        b.push("terrain");
        TERRAIN_SCALE = b.defineInRange("terrainScale", 1.0, 0.25, 4.0);
        DISABLE_NOISE_WITH_TERRAIN_MODS = b.comment("Disable custom noise settings when Terralith/Tellus is present").define("disableNoiseWithTerrainMods", true);
        STRUCTURES_IN_NEW = b.define("generateInVanillaStructures", false);
        b.pop();
        b.push("mobs");
        SPAWN_MULT = b.defineInRange("mobSpawnMultiplier", 1.0, 0.0, 5.0);
        b.pop();
        ALIASES = b.define("registerLegacyAliases", true);
        FIRE_IMMUNE_GEAR = b.define("fireImmuneEmberGear", true);
        SPEC = b.build();
    }

    private NetherExConfig() {}
}
