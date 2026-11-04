package net.diaithir.portable;

import java.util.ArrayList;
import java.util.List;

/** Pure logic shared by blocks and items: stack-size maths, sorting order, and DeepNull accounting. No Minecraft types, so the selftest can run it. */
public final class Rules {
    public static final long DEEP_CAP = 2_000_000_000L;

    private Rules() {
    }

    /** Effective stack size after n stack upgrades, saturating at 1,000,000. */
    public static int stackSize(int base, int upgrades, int multiplier) {
        long s = base;
        for (int i = 0; i < upgrades; i++) {
            s *= multiplier;
            if (s >= 1_000_000L) return 1_000_000;
        }
        return (int) s;
    }

    /** Adds to a DeepNull count, returns the amount actually accepted. */
    public static long deepAdd(long have, long add) {
        long room = DEEP_CAP - have;
        return Math.max(0, Math.min(room, add));
    }

    /** Removes from a DeepNull count, returns the amount actually removed. */
    public static long deepTake(long have, long want) {
        return Math.max(0, Math.min(have, want));
    }

    /** Cycles a mode index through [0,count). */
    public static int cycle(int mode, int count) {
        return (mode + 1) % count;
    }

    /** Sorted copy of ids, alphabetical, used by the sort button for stable ordering. */
    public static List<String> sortedIds(List<String> ids) {
        List<String> out = new ArrayList<>(ids);
        out.sort(String::compareTo);
        return out;
    }
}
