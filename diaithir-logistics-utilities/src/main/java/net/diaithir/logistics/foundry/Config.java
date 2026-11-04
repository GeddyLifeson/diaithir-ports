package net.diaithir.logistics.foundry;

import net.neoforged.neoforge.common.ModConfigSpec;

/** Server config (diaithirlogistics-server.toml). Read only at runtime, after the world has loaded. */
public final class Config {
    private Config() {}

    private static final ModConfigSpec.Builder B = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue SERVICE_RADIUS = B
            .comment("How far (in blocks) a depot reaches to serve ghosts.")
            .defineInRange("serviceRadius", 48, 4, 256);
    public static final ModConfigSpec.IntValue COURIERS_PER_DEPOT = B
            .comment("Couriers a depot keeps. Each carries one block's item per trip.")
            .defineInRange("couriersPerDepot", 4, 1, 64);
    public static final ModConfigSpec.DoubleValue COURIER_SPEED = B
            .comment("Courier speed in blocks per tick.")
            .defineInRange("courierBlocksPerTick", 0.5, 0.05, 8.0);
    public static final ModConfigSpec.IntValue RUN_INTERVAL = B
            .comment("Ticks between depot runs. Higher is lighter.")
            .defineInRange("depotIntervalTicks", 10, 1, 200);
    public static final ModConfigSpec.IntValue CLAIM_TIMEOUT = B
            .comment("Ticks before a claim on a ghost lapses (covers couriers lost to a restart).")
            .defineInRange("claimTimeoutTicks", 1200, 100, 72000);

    public static final ModConfigSpec SPEC = B.build();
}
