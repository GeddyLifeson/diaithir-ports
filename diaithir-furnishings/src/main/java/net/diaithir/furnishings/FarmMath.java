package net.diaithir.furnishings;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Pure maths for farm implements (no Minecraft types, unit tested). SI: metres, seconds, watts, joules. */
final class FarmMath {
    /** Working speed in m/s: walking pace by hand, ox pace behind animals, tractor pace for engines. */
    static final double HAND_SPEED = 1.0, DRAFT_SPEED = 1.0, POWERED_SPEED = 2.0;
    /** Swath length in metres per use. */
    static final int HAND_LEN = 3, DRAFT_LEN = 6, POWERED_LEN = 10;
    /** Energy per fuel item in joules (1 kg of the fuel). */
    static final Map<String, Long> FUEL_J = Map.of(
            "minecraft:coal", 24_000_000L, "minecraft:charcoal", 29_000_000L, "minecraft:coal_block", 216_000_000L,
            "minecraft:dried_kelp_block", 18_000_000L, "minecraft:blaze_rod", 24_000_000L, "minecraft:stick", 1_000_000L);

    private FarmMath() {}

    static int length(String mode) {
        return mode.equals("powered") ? POWERED_LEN : mode.equals("draft") ? DRAFT_LEN : HAND_LEN;
    }

    static double speed(String mode) {
        return mode.equals("powered") ? POWERED_SPEED : mode.equals("draft") ? DRAFT_SPEED : HAND_SPEED;
    }

    /** Energy in joules one swath costs: rated power times the time it takes to cover the length. */
    static long swathJoules(int powerW, String mode, double efficiency) {
        return Math.round(powerW * length(mode) / speed(mode) / Math.max(0.1, efficiency));
    }

    /** Offsets (forward, sideways) of a swath, width centred on the player's line. */
    static List<int[]> swath(int width, int length) {
        List<int[]> out = new ArrayList<>();
        int half = (width - 1) / 2;
        for (int f = 0; f < length; f++)
            for (int s = -half; s < width - half; s++) out.add(new int[] {f, s});
        return out;
    }

    /** Tank durability points restored by fuel of the given energy (1 point = 1 kJ). */
    static int tankPoints(long joules) {
        return (int) Math.min(Integer.MAX_VALUE, joules / 1000);
    }
}
