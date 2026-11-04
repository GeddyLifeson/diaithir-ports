package net.diaithir.portable;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class Config {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.IntValue MAGNET_RANGE;
    public static final ModConfigSpec.IntValue ITEMS_PER_TICK;
    public static final ModConfigSpec.IntValue STACK_MULTIPLIER;
    public static final ModConfigSpec.IntValue FEED_INTERVAL;
    public static final ModConfigSpec.IntValue SMELT_TICKS;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        MAGNET_RANGE = b.comment("Radius in blocks of the magnet and pickup upgrades").defineInRange("magnetRange", 6, 1, 32);
        ITEMS_PER_TICK = b.comment("Most item entities a pickup or magnet upgrade moves per tick, a throughput limit").defineInRange("itemsPerTick", 4, 1, 64);
        STACK_MULTIPLIER = b.comment("Stack size multiplier per stack upgrade").defineInRange("stackMultiplier", 2, 2, 16);
        FEED_INTERVAL = b.comment("Ticks between feeding upgrade checks").defineInRange("feedInterval", 20, 1, 400);
        SMELT_TICKS = b.comment("Ticks one item takes in a smelting upgrade").defineInRange("smeltTicks", 100, 1, 2000);
        SPEC = b.build();
    }

    private Config() {
    }
}
