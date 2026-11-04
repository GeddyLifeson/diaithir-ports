package net.diaithir.chunkloaders;

import net.neoforged.neoforge.common.ModConfigSpec;

/** Server config (serverconfig/diaithirchunkloaders-server.toml). The caps are what keep a server's TPS safe. */
final class Config {
    static final ModConfigSpec SPEC;
    static final ModConfigSpec.IntValue MAX_PER_PLAYER, FUEL_MULTIPLIER, MAX_HOURS;
    static final ModConfigSpec.IntValue[] RADIUS = new ModConfigSpec.IntValue[4], COST = new ModConfigSpec.IntValue[4];
    static final ModConfigSpec.BooleanValue OPS_BYPASS_CAP, ENTITY_TICKING, CARTS_ENABLED;
    static final ModConfigSpec.IntValue CART_MAX_PER_PLAYER, CART_RADIUS, CART_MINUTES_PER_SEAL, CART_MAX_MINUTES;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        b.push("loaders");
        MAX_PER_PLAYER = b.comment("Most loader blocks one player may own (placing more is refused). 0 means none allowed.").defineInRange("maxLoadersPerPlayer", 4, 0, 256);
        OPS_BYPASS_CAP = b.comment("Operators ignore the cap.").define("opsBypassCap", true);
        ENTITY_TICKING = b.comment("true: forced chunks tick fully (crops, machines, mobs). false: chunks are only kept loaded, which costs much less.").define("entityTicking", true);
        FUEL_MULTIPLIER = b.comment("Loader-ticks of fuel per vanilla burn tick (a coal burns 1600 ticks).").defineInRange("fuelMultiplier", 10, 1, 1000);
        MAX_HOURS = b.comment("Most hours of fuel one loader may hold, counted at its own burn rate.").defineInRange("maxFuelHours", 48, 1, 10000);
        int[] radius = {0, 0, 1, 2}, cost = {0, 1, 2, 4};
        for (int t = 1; t <= 3; t++) {
            b.push("tier" + t);
            RADIUS[t] = b.comment("Chunk radius kept loaded: 0 is the loader's own chunk, 1 is 3x3, 2 is 5x5.").defineInRange("radius", radius[t], 0, 4);
            COST[t] = b.comment("Fuel ticks burned per game tick.").defineInRange("fuelPerTick", cost[t], 1, 1000);
            b.pop();
        }
        b.pop();
        b.push("carts");
        CARTS_ENABLED = b.comment("Optional tier: a Cartseal on a minecart keeps the chunks around the cart loaded while it rides.").define("enabled", true);
        CART_MAX_PER_PLAYER = b.defineInRange("maxCartsPerPlayer", 2, 0, 64);
        CART_RADIUS = b.comment("Chunk radius around the cart.").defineInRange("radius", 1, 0, 3);
        CART_MINUTES_PER_SEAL = b.defineInRange("minutesPerSeal", 30, 1, 1440);
        CART_MAX_MINUTES = b.defineInRange("maxMinutes", 180, 1, 10000);
        b.pop();
        SPEC = b.build();
    }

    private Config() {}
}
