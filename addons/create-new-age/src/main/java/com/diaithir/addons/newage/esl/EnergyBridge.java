package com.diaithir.addons.newage.esl;

import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/** Converts between the 64-bit storage and NeoForge's 32-bit energy handler (FE). */
public final class EnergyBridge {
    private EnergyBridge() {}

    private static int clamp(long v) { return (int) Math.max(0, Math.min(Integer.MAX_VALUE, v)); }

    public static EnergyHandler asHandler(EnergyStorage s) {
        if (s == null) return null;
        if (s instanceof Wrapped w) return w.h;
        return new EnergyHandler() {
            public long getAmountAsLong() { return s.getStoredEnergy(); }
            public long getCapacityAsLong() { return s.getCapacity(); }
            public int insert(int amount, TransactionContext tx) { return s.supportsInsertion() ? clamp(s.insert(amount, tx)) : 0; }
            public int extract(int amount, TransactionContext tx) { return s.supportsExtraction() ? clamp(s.extract(amount, tx)) : 0; }
        };
    }

    public static EnergyStorage asStorage(EnergyHandler h) {
        return new Wrapped(h);
    }

    private record Wrapped(EnergyHandler h) implements EnergyStorage {
        public long insert(long max, TransactionContext tx) { return h.insert(clamp(max), tx); }
        public long extract(long max, TransactionContext tx) { return h.extract(clamp(max), tx); }
        public long getStoredEnergy() { return h.getAmountAsLong(); }
        public long getCapacity() { return h.getCapacityAsLong(); }
    }
}
