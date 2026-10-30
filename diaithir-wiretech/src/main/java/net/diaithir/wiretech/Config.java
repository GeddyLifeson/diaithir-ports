package net.diaithir.wiretech;

import net.neoforged.neoforge.common.ModConfigSpec;

/** Server config (diaithir_wiretech-server.toml). */
public final class Config {
    private Config() {}

    private static final ModConfigSpec.Builder B = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue BASE_CAPACITY = B.comment("FE buffer of a machine.")
            .defineInRange("baseCapacity", 20000, 1000, 100000000);
    public static final ModConfigSpec.IntValue BASE_RECEIVE = B.comment("Most FE per tick a machine accepts.")
            .defineInRange("baseReceive", 400, 1, 100000000);
    public static final ModConfigSpec.IntValue TANK = B.comment("Tank size in mB for machines that have one.")
            .defineInRange("tank", 8000, 1000, 1000000);
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

    public static final ModConfigSpec.IntValue CRANK_TORQUE = B.comment("Torque a hand crank gives for CRANK_TICKS after each turn.")
            .defineInRange("crankTorque", 8, 1, 1000);
    public static final ModConfigSpec.IntValue CRANK_TICKS = B.comment("How long one turn of the hand crank lasts, in ticks.")
            .defineInRange("crankTicks", 20, 1, 1200);
    public static final ModConfigSpec.IntValue WINDMILL_TORQUE = B.comment("Torque of a windmill with a fully open frame at sea level.")
            .defineInRange("windmillTorque", 6, 1, 1000);
    public static final ModConfigSpec.IntValue WATERWHEEL_TORQUE = B.comment("Torque a water wheel gets from each side that touches flowing water.")
            .defineInRange("waterWheelTorque", 3, 1, 1000);
    public static final ModConfigSpec.IntValue FE_PER_TORQUE = B.comment("FE per tick a dynamo makes for each point of torque.")
            .defineInRange("fePerTorque", 8, 1, 100000);
    public static final ModConfigSpec.DoubleValue WIRE_LOSS_SCALE = B.comment("Multiplier on every wire's loss per block (0 turns loss off).")
            .defineInRange("wireLossScale", 1.0, 0.0, 100.0);
    public static final ModConfigSpec.DoubleValue WIRE_RANGE_SCALE = B.comment("Multiplier on every wire's maximum length.")
            .defineInRange("wireRangeScale", 1.0, 0.1, 16.0);

    /** Used before the config loads (and on a client), never read for gameplay. */
    public static final int BASE_CAPACITY_DEFAULT = 20000, BASE_RECEIVE_DEFAULT = 400, TANK_DEFAULT = 8000;

    public static final ModConfigSpec SPEC = B.build();

    public static double tierSpeed(int tier) {
        return 1.0;
    }

    public static int tierMult(int tier) {
        return 1 << tier;
    }
}
