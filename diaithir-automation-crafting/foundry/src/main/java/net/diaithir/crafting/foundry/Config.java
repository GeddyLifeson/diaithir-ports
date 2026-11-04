package net.diaithir.crafting.foundry;

import net.neoforged.neoforge.common.ModConfigSpec;

/** Server config (diaithircrafting-server.toml). */
public final class Config {
    private Config() {}

    private static final ModConfigSpec.Builder B = new ModConfigSpec.Builder();

    public static final ModConfigSpec.DoubleValue SECONDS_PER_NUTRITION = B
            .comment("Seconds of work one point of food nutrition buys. Bread (5) buys 10 minutes at the default.")
            .defineInRange("secondsPerNutrition", 120.0, 1.0, 100000.0);

    public static final ModConfigSpec.IntValue RANGE = B
            .comment("Blocks around a machine in which an Operator Seat can serve it.")
            .defineInRange("seatRange", 8, 1, 32);

    public static final ModConfigSpec.IntValue FOOD_ITEMS = B
            .comment("Food items one Operator Seat holds (one kind of food at a time).")
            .defineInRange("foodItems", 64, 1, 4096);

    public static final ModConfigSpec SPEC = B.build();
}
