package net.diaithir.travel.laser;

import java.util.List;
import net.neoforged.neoforge.common.ModConfigSpec;

/** Server config (replaces the upstream Deimos config library, which is not folded in). */
public final class Config {
    private static final ModConfigSpec.Builder B = new ModConfigSpec.Builder();
    private static final ModConfigSpec.IntValue MAX_LENGTH = B.comment("Bridge length in metres at maximum redstone power (1 block = 1 m)")
            .defineInRange("max_length", 15, 1, 64);
    private static final ModConfigSpec.ConfigValue<List<? extends String>> CUT = B.comment("Blocks a laser replaces while it grows")
            .defineList("blocks_cut_through_by_lasers", List.of("minecraft:air", "minecraft:cave_air", "minecraft:void_air", "minecraft:short_grass",
                    "minecraft:tall_grass", "minecraft:fern", "minecraft:large_fern", "minecraft:dead_bush"), () -> "minecraft:air", o -> o instanceof String);
    public static final ModConfigSpec SPEC = B.build();

    private Config() {}

    public static int maxLength() {
        return SPEC.isLoaded() ? MAX_LENGTH.get() : 15;
    }

    public static boolean cutsThrough(String blockId) {
        return SPEC.isLoaded() ? CUT.get().contains(blockId) : blockId.endsWith("air");
    }
}
