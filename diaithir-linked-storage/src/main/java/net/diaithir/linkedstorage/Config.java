package net.diaithir.linkedstorage;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class Config {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.IntValue ITEMS_PER_OPERATION;
    public static final ModConfigSpec.IntValue FLUID_PER_OPERATION;
    public static final ModConfigSpec.IntValue ENERGY_PER_OPERATION;
    public static final ModConfigSpec.IntValue TANK_CAPACITY;
    public static final ModConfigSpec.IntValue CELL_CAPACITY;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        ITEMS_PER_OPERATION = b.comment("Most items a hopper or pipe may move through one linked chest per operation.").defineInRange("itemsPerOperation", 64, 1, 4096);
        FLUID_PER_OPERATION = b.comment("Most millibuckets that may move through a linked tank per operation.").defineInRange("fluidPerOperation", 4000, 1, 1000000);
        ENERGY_PER_OPERATION = b.comment("Most energy that may move through a linked cell per operation.").defineInRange("energyPerOperation", 20000, 1, 100000000);
        TANK_CAPACITY = b.comment("Capacity of a shared tank in millibuckets.").defineInRange("tankCapacity", 64000, 1000, 100000000);
        CELL_CAPACITY = b.comment("Capacity of a shared cell.").defineInRange("cellCapacity", 10000000, 1000, 2000000000);
        SPEC = b.build();
    }

    private Config() {}
}
