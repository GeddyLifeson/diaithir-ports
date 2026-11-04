package org.diaithir.archimedes;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class ArchimedesConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.IntValue MAX_BLOCKS;
    public static final ModConfigSpec.DoubleValue FLY_BALLOON_RATIO, SPEED_LIMIT, TURN_SPEED;
    public static final ModConfigSpec.BooleanValue ENABLE_AIRSHIPS, DISASSEMBLE_ON_DISMOUNT, DECK_WALK;

    static {
        var b = new ModConfigSpec.Builder();
        MAX_BLOCKS = b.comment("Largest ship, in blocks.").defineInRange("maxShipBlocks", 2048, 1, 16384);
        ENABLE_AIRSHIPS = b.define("enableAirships", true);
        FLY_BALLOON_RATIO = b.comment("Share of balloons needed to lift a ship.").defineInRange("flyBalloonRatio", 0.5, 0.01, 1.0);
        SPEED_LIMIT = b.comment("Blocks per tick.").defineInRange("speedLimit", 0.5, 0.05, 2.0);
        TURN_SPEED = b.defineInRange("turnSpeed", 1.0, 0.1, 5.0);
        DISASSEMBLE_ON_DISMOUNT = b.define("disassembleOnDismount", true);
        DECK_WALK = b.comment("Entities standing on a deck are carried along when the ship moves and turns.").define("deckWalk", true);
        SPEC = b.build();
    }

    private ArchimedesConfig() {}
}
