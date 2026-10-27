package net.diaithir.forcefield;

import net.neoforged.neoforge.common.ModConfigSpec;

/** Server config (diaithirforcefield-server.toml). Read only at runtime, after the world has loaded. */
public final class Config {
    private Config() {}

    private static final ModConfigSpec.Builder B = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue MAX_FIELD_BLOCKS = B
            .comment("Most blocks one projector may place. A shape that would need more is shrunk until it fits.")
            .defineInRange("maxFieldBlocks", 12000, 100, 200000);
    public static final ModConfigSpec.IntValue BASE_RANGE = B.comment("Radius of a projector with no range modules.")
            .defineInRange("baseRange", 6, 1, 64);
    public static final ModConfigSpec.IntValue RANGE_STEP = B.comment("Radius added by each range module.")
            .defineInRange("rangeStep", 4, 1, 32);
    public static final ModConfigSpec.IntValue MAX_RANGE = B.comment("Largest radius any projector can reach.")
            .defineInRange("maxRange", 32, 1, 64);
    public static final ModConfigSpec.IntValue MAX_PER_MODULE = B.comment("How many of each module a projector takes.")
            .defineInRange("maxPerModule", 4, 1, 16);
    public static final ModConfigSpec.IntValue MAX_THICKNESS = B.comment("Most layers a shell can have (1 plus strength modules).")
            .defineInRange("maxThickness", 4, 1, 8);
    public static final ModConfigSpec.IntValue UPKEEP = B
            .comment("FE per tick the field costs for every 1000 blocks of shell (at least 1 FE per tick).")
            .defineInRange("upkeepPerThousandBlocks", 20, 0, 100000);
    public static final ModConfigSpec.IntValue BASE_CAPACITY = B.comment("FE buffer of a projector with no capacitor modules.")
            .defineInRange("baseCapacity", 40000, 1000, 100000000);
    public static final ModConfigSpec.IntValue CAPACITOR_BONUS = B.comment("FE of buffer added by each capacitor module.")
            .defineInRange("capacitorBonus", 40000, 0, 100000000);
    public static final ModConfigSpec.IntValue MAX_RECEIVE = B.comment("Most FE per tick the projector accepts.")
            .defineInRange("maxReceive", 4000, 1, 100000000);
    public static final ModConfigSpec.IntValue CHECK_INTERVAL = B
            .comment("Ticks between field passes (power draw, intruder check). Higher is lighter, lower holds fast intruders better.")
            .defineInRange("checkIntervalTicks", 4, 1, 40);
    public static final ModConfigSpec.IntValue BUILD_PER_PASS = B
            .comment("Field blocks placed or removed per pass, so a big shell grows over a few passes instead of in one tick.")
            .defineInRange("blocksPerPass", 3000, 100, 200000);
    public static final ModConfigSpec.BooleanValue ALLOW_STING = B.comment("Let the sting module hurt blocked intruders.")
            .define("allowIntruderDamage", true);
    public static final ModConfigSpec.DoubleValue STING_DAMAGE = B.comment("Damage per pass dealt to a blocked intruder by the sting module.")
            .defineInRange("intruderDamage", 2.0, 0.0, 100.0);
    public static final ModConfigSpec.BooleanValue CREATIVE_BYPASS = B.comment("Creative and spectator players pass every field.")
            .define("creativeBypass", true);
    public static final ModConfigSpec.BooleanValue BLOCK_NEUTRAL = B
            .comment("Also block animals, villagers and other non-hostile mobs that are not the owner's pets.")
            .define("blockNonHostileMobs", false);

    public static final ModConfigSpec SPEC = B.build();
}
