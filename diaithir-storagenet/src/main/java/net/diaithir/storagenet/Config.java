package net.diaithir.storagenet;

import net.neoforged.neoforge.common.ModConfigSpec;

/** Server config (diaithirstoragenet-server.toml). Read only at runtime, after the world has loaded. */
public final class Config {
    private Config() {}

    private static final ModConfigSpec.Builder B = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue MAX_NETWORK_BLOCKS = B
            .comment("Most network blocks (cables, terminals) one controller will walk. The scan stops there, so a runaway cable cannot stall the server.")
            .defineInRange("maxNetworkBlocks", 600, 1, 20000);
    public static final ModConfigSpec.IntValue MAX_STORES = B
            .comment("Most inventories one network will use as storage.")
            .defineInRange("maxInventories", 200, 1, 5000);
    public static final ModConfigSpec.IntValue OPERATION_INTERVAL = B
            .comment("Ticks between runs of an import or export cable. Higher is lighter.")
            .defineInRange("cableIntervalTicks", 10, 1, 1200);
    public static final ModConfigSpec.IntValue ITEMS_PER_OPERATION = B
            .comment("Most items one import or export cable moves per run.")
            .defineInRange("itemsPerRun", 16, 1, 4096);
    public static final ModConfigSpec.IntValue FILTER_SLOTS = B
            .comment("Items one cable filter can hold.")
            .defineInRange("filterSlots", 9, 1, 27);
    public static final ModConfigSpec.IntValue MAX_TERMINAL_DISTANCE = B
            .comment("How far (in blocks) a player may stand from an open terminal.")
            .defineInRange("terminalReach", 8, 2, 64);

    public static final ModConfigSpec SPEC = B.build();
}
