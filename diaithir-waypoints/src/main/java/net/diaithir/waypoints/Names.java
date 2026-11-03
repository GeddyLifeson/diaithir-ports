package net.diaithir.waypoints;

import net.minecraft.core.BlockPos;

/** A stable name for a stone, derived only from its position so every player sees the same one. */
public final class Names {
    private static final String[] A = {"Amber", "Ash", "Bright", "Cold", "Dawn", "Deep", "Elder", "Fern", "Grey", "High", "Iron", "Long", "Mist", "Moss", "Old", "Pale", "Red", "Salt", "Slate", "Still", "Thorn", "Wild"};
    private static final String[] B = {"Barrow", "Brook", "Cairn", "Crag", "Fell", "Ford", "Glen", "Hollow", "Keep", "Lake", "Mere", "Moor", "Pass", "Ridge", "Rise", "Shore", "Stone", "Vale", "Watch", "Wood"};

    private Names() {}

    public static String of(BlockPos pos) {
        long h = pos.asLong() * 0x9E3779B97F4A7C15L;
        h ^= h >>> 29;
        int a = (int) Math.floorMod(h, (long) A.length);
        int b = (int) Math.floorMod(h >>> 20, (long) B.length);
        return A[a] + " " + B[b];
    }
}
