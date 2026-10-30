package net.diaithir.conduits;

import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.List;

/** Moves things between handlers without ever losing any: first learn how much fits, then move exactly that in one transaction. */
final class Moves {
    private Moves() {}

    /** Moves up to max units from one handler into the first targets that will take them. Returns how many moved. */
    static <T extends Resource> int move(ResourceHandler<T> from, List<ResourceHandler<T>> to, int max) {
        int moved = 0;
        for (int i = 0, n = from.size(); i < n && moved < max; i++) {
            T r = from.getResource(i);
            long have = from.getAmountAsLong(i);
            if (r.isEmpty() || have <= 0) continue;
            for (ResourceHandler<T> t : to) {
                int want = (int) Math.min(have, max - moved);
                if (want <= 0) break;
                int m = one(from, t, r, want);
                moved += m;
                have -= m;
            }
        }
        return moved;
    }

    private static <T extends Resource> int one(ResourceHandler<T> from, ResourceHandler<T> to, T r, int amount) {
        int fit;
        try (Transaction tx = Transaction.openRoot()) {
            int out = from.extract(r, amount, tx);
            fit = out <= 0 ? 0 : to.insert(r, out, tx);
        }
        if (fit <= 0) return 0;
        try (Transaction tx = Transaction.openRoot()) {
            int out = from.extract(r, fit, tx);
            int in = out <= 0 ? 0 : to.insert(r, out, tx);
            if (out == fit && in == fit) {
                tx.commit();
                return fit;
            }
        }
        return 0;
    }

    static int moveEnergy(EnergyHandler from, List<EnergyHandler> to, int max) {
        int moved = 0;
        for (EnergyHandler t : to) {
            int want = max - moved;
            if (want <= 0) break;
            int fit;
            try (Transaction tx = Transaction.openRoot()) {
                int out = from.extract(want, tx);
                fit = out <= 0 ? 0 : t.insert(out, tx);
            }
            if (fit <= 0) continue;
            try (Transaction tx = Transaction.openRoot()) {
                int out = from.extract(fit, tx);
                int in = out <= 0 ? 0 : t.insert(out, tx);
                if (out == fit && in == fit) {
                    tx.commit();
                    moved += fit;
                }
            }
        }
        return moved;
    }

    /** A handler that accepts anything and hands it on. Nothing is ever stored in it and nothing comes out. */
    static final class Sink<T extends Resource> implements ResourceHandler<T> {
        interface Deliver<T extends Resource> {
            int send(T resource, int amount, TransactionContext tx);
        }

        private final T empty;
        private final Deliver<T> deliver;

        Sink(T empty, Deliver<T> deliver) {
            this.empty = empty;
            this.deliver = deliver;
        }

        @Override public int size() { return 1; }
        @Override public T getResource(int index) { return empty; }
        @Override public long getAmountAsLong(int index) { return 0; }
        @Override public long getCapacityAsLong(int index, T resource) { return Integer.MAX_VALUE; }
        @Override public boolean isValid(int index, T resource) { return index == 0; }
        @Override public int insert(int index, T resource, int amount, TransactionContext tx) { return index == 0 && amount > 0 ? deliver.send(resource, amount, tx) : 0; }
        @Override public int extract(int index, T resource, int amount, TransactionContext tx) { return 0; }
    }

    static final class EnergySink implements EnergyHandler {
        private final java.util.function.BiFunction<Integer, TransactionContext, Integer> deliver;

        EnergySink(java.util.function.BiFunction<Integer, TransactionContext, Integer> deliver) {
            this.deliver = deliver;
        }

        @Override public long getAmountAsLong() { return 0; }
        @Override public long getCapacityAsLong() { return Integer.MAX_VALUE; }
        @Override public int insert(int amount, TransactionContext tx) { return amount > 0 ? deliver.apply(amount, tx) : 0; }
        @Override public int extract(int amount, TransactionContext tx) { return 0; }
    }
}
