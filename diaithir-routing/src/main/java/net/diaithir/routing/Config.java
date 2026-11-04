package net.diaithir.routing;

import net.neoforged.neoforge.common.ModConfigSpec;

/** Server config: throughput and range limits. */
public final class Config {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.IntValue ITEMS_PER_OPERATION;
    public static final ModConfigSpec.IntValue FLUID_PER_OPERATION;
    public static final ModConfigSpec.IntValue ENERGY_PER_OPERATION;
    public static final ModConfigSpec.IntValue OPERATION_INTERVAL;
    public static final ModConfigSpec.IntValue BASE_RANGE;
    public static final ModConfigSpec.IntValue RANGE_PER_CARD;
    public static final ModConfigSpec.IntValue LASER_RANGE;
    public static final ModConfigSpec.IntValue HOPPER_INTERVAL;
    public static final ModConfigSpec.IntValue FAST_HOPPER_MULTIPLIER;
    public static final ModConfigSpec.IntValue COLLECTOR_MAX_RANGE;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        b.push("throughput");
        ITEMS_PER_OPERATION = b.comment("Items moved per module per run").defineInRange("itemsPerOperation", 16, 1, 4096);
        FLUID_PER_OPERATION = b.comment("Millibuckets moved per module per run").defineInRange("fluidPerOperation", 1000, 1, 1000000);
        ENERGY_PER_OPERATION = b.comment("Energy units moved per module per run").defineInRange("energyPerOperation", 4000, 1, 100000000);
        OPERATION_INTERVAL = b.comment("Ticks between router and laser node runs").defineInRange("operationInterval", 10, 1, 1200);
        b.pop();
        b.push("range");
        BASE_RANGE = b.comment("Wireless range of a router with no range cards").defineInRange("baseRange", 16, 1, 256);
        RANGE_PER_CARD = b.comment("Extra range per range card").defineInRange("rangePerCard", 16, 0, 256);
        LASER_RANGE = b.comment("Maximum laser node distance").defineInRange("laserRange", 16, 1, 256);
        b.pop();
        b.push("hoppers");
        HOPPER_INTERVAL = b.comment("Ticks between hopper transfers").defineInRange("hopperInterval", 8, 1, 200);
        FAST_HOPPER_MULTIPLIER = b.comment("Speed multiplier of the fast hopper").defineInRange("fastHopperMultiplier", 3, 1, 64);
        COLLECTOR_MAX_RANGE = b.comment("Largest pick-up range of the item collector").defineInRange("collectorMaxRange", 16, 1, 64);
        b.pop();
        SPEC = b.build();
    }

    private Config() {
    }
}
