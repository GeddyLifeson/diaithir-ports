package net.diaithir.forgeworks;

import net.neoforged.neoforge.common.ModConfigSpec;

/** Server config (diaithirforgeworks-server.toml). */
public final class Config {
    private Config() {}

    private static final ModConfigSpec.Builder B = new ModConfigSpec.Builder();

    public static final ModConfigSpec.DoubleValue SPEED_AGE5 = B.comment("Speed multiplier of an Age V casing (Age IV is 1).")
            .defineInRange("speedAge5", 2.0, 0.1, 100.0);
    public static final ModConfigSpec.DoubleValue SPEED_AGE6 = B.comment("Speed multiplier of an Age VI casing.")
            .defineInRange("speedAge6", 4.0, 0.1, 100.0);
    public static final ModConfigSpec.IntValue BASE_CAPACITY = B.comment("FE buffer of an Age IV machine. Age V has double, Age VI quadruple.")
            .defineInRange("baseCapacity", 20000, 1000, 100000000);
    public static final ModConfigSpec.IntValue BASE_RECEIVE = B.comment("Most FE per tick an Age IV machine accepts. Scales with the tier like the buffer.")
            .defineInRange("baseReceive", 400, 1, 100000000);
    public static final ModConfigSpec.IntValue TANK = B.comment("Crucible tank size in mB.")
            .defineInRange("crucibleTank", 8000, 1000, 1000000);
    public static final ModConfigSpec.IntValue MAX_AUGMENTS = B.comment("How many augments fit in the augment slot (one kind at a time).")
            .defineInRange("maxAugments", 4, 1, 64);
    public static final ModConfigSpec.DoubleValue SPEED_PER_AUGMENT = B.comment("Speed added by each speed augment, as a fraction (0.5 is +50%).")
            .defineInRange("speedPerAugment", 0.5, 0.0, 10.0);
    public static final ModConfigSpec.DoubleValue SPEED_ENERGY_PENALTY = B.comment("Extra FE per recipe for each speed augment, as a fraction.")
            .defineInRange("speedEnergyPenalty", 0.1, 0.0, 10.0);
    public static final ModConfigSpec.DoubleValue SAVING_PER_AUGMENT = B.comment("FE saved by each efficiency augment, as a fraction.")
            .defineInRange("savingPerAugment", 0.15, 0.0, 0.24);
    public static final ModConfigSpec.DoubleValue CHANCE_PER_AUGMENT = B.comment("Bonus chance is multiplied by 1 plus this for each output chance augment.")
            .defineInRange("chancePerAugment", 0.5, 0.0, 100.0);

    /** Used before the config loads (and on a client), never read for gameplay. */
    public static final int BASE_CAPACITY_DEFAULT = 20000, BASE_RECEIVE_DEFAULT = 400, TANK_DEFAULT = 8000;

    public static final ModConfigSpec SPEC = B.build();

    /** Speed multiplier for a tier 0 to 2. */
    public static double tierSpeed(int tier) {
        return tier == 0 ? 1.0 : tier == 1 ? SPEED_AGE5.get() : SPEED_AGE6.get();
    }

    public static int tierMult(int tier) {
        return 1 << tier;
    }
}
