package net.diaithir.conduits;

import net.neoforged.neoforge.common.ModConfigSpec;

/** Server config (diaithirconduits-server.toml). Read only at runtime, after the world has loaded. */
public final class Config {
    private Config() {}

    private static final ModConfigSpec.Builder B = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue MAX_CHANNELS_PER_PLAYER = B
            .comment("Most channels one player may own (a channel counts while at least one node is on it). Operators ignore this when joining their own channels.")
            .defineInRange("maxChannelsPerPlayer", 8, 1, 1000);
    public static final ModConfigSpec.IntValue MAX_NODES_PER_CHANNEL = B
            .comment("Most nodes on one channel.")
            .defineInRange("maxNodesPerChannel", 32, 2, 1000);
    public static final ModConfigSpec.IntValue MAX_CHANNEL_NUMBER = B
            .comment("Channels are numbered 1 to this.")
            .defineInRange("maxChannelNumber", 9999, 1, 1000000);
    public static final ModConfigSpec.IntValue INTERVAL = B
            .comment("Ticks between runs of a node. All bandwidth limits are per run. Higher is lighter.")
            .defineInRange("intervalTicks", 8, 1, 1200);
    public static final ModConfigSpec.IntValue ITEMS_PER_RUN = B
            .comment("Items one tier 1 node may send per run.")
            .defineInRange("itemsPerRun", 16, 1, 100000);
    public static final ModConfigSpec.IntValue FLUID_PER_RUN = B
            .comment("Millibuckets one tier 1 node may send per run.")
            .defineInRange("fluidPerRun", 1000, 1, 10000000);
    public static final ModConfigSpec.IntValue ENERGY_PER_RUN = B
            .comment("FE one tier 1 node may send per run.")
            .defineInRange("energyPerRun", 4000, 1, 100000000);
    public static final ModConfigSpec.IntValue TIER2_MULTIPLIER = B
            .comment("Bandwidth multiplier for a tier 2 node.")
            .defineInRange("tier2Multiplier", 8, 1, 1000);
    public static final ModConfigSpec.BooleanValue OPS_MANAGE_ALL = B
            .comment("Operators may configure and break any node and join any channel.")
            .define("opsManageAll", true);
    public static final ModConfigSpec.IntValue FLUID_CELL_CAPACITY = B
            .comment("Millibuckets a fluid cell holds.")
            .defineInRange("fluidCellCapacity", 32000, 1000, 100000000);
    public static final ModConfigSpec.IntValue ENERGY_CELL_CAPACITY = B
            .comment("FE an energy cell holds.")
            .defineInRange("energyCellCapacity", 500000, 1000, 1000000000);
    public static final ModConfigSpec.IntValue CELL_TRANSFER = B
            .comment("Most FE or mB a cell accepts or gives per single operation.")
            .defineInRange("cellTransfer", 20000, 1, 100000000);

    public static final ModConfigSpec SPEC = B.build();

    public static int mult(int tier) {
        return tier >= 2 ? TIER2_MULTIPLIER.get() : 1;
    }
}
