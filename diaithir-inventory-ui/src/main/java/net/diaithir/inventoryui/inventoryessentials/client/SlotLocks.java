package net.diaithir.inventoryui.inventoryessentials.client;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;

public final class SlotLocks {
    private static final IntOpenHashSet LOCKED = new IntOpenHashSet();

    private SlotLocks() {}

    public static boolean isLocked(int invSlot) {
        return LOCKED.contains(invSlot);
    }

    public static void toggle(int invSlot) {
        if (!LOCKED.remove(invSlot)) LOCKED.add(invSlot);
    }

    public static int[] snapshot() {
        return LOCKED.toIntArray();
    }

    public static IntOpenHashSet raw() {
        return LOCKED;
    }

    public static void clear() {
        LOCKED.clear();
    }
}
