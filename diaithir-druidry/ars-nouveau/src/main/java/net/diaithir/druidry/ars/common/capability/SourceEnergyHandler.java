package net.diaithir.druidry.ars.common.capability;

import net.diaithir.druidry.ars.api.source.ISourceCap;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.function.Supplier;

/**
 * Presents stored Source as a plain NeoForge energy handler at the pack rate of 1 Source = 1000 J (1 FE = 1 J), so a jar of N Source reports
 * N * 1000 FE. Extract-only: Source is never made from FE, and only whole Source is taken (a request of 999 FE moves nothing).
 * Same rate as diaithir-units magic_fuel.csv / fuels.csv (Source is ten mana at 100 J).
 */
public final class SourceEnergyHandler implements EnergyHandler {
    public static final int J_PER_SOURCE = 1000;

    private final Supplier<? extends ISourceCap> store;
    private final Journal journal = new Journal();

    public SourceEnergyHandler(Supplier<? extends ISourceCap> store) {
        this.store = store;
    }

    @Override
    public long getAmountAsLong() {
        return (long) store.get().getSource() * J_PER_SOURCE;
    }

    @Override
    public long getCapacityAsLong() {
        return (long) store.get().getSourceCapacity() * J_PER_SOURCE;
    }

    @Override
    public int insert(int amount, TransactionContext transaction) {
        return 0;
    }

    @Override
    public int extract(int amount, TransactionContext transaction) {
        if (amount < 0) throw new IllegalArgumentException("negative amount");
        ISourceCap s = store.get();
        int units = s.extractSource(amount / J_PER_SOURCE, true);
        if (units <= 0) return 0;
        journal.updateSnapshots(transaction);
        return s.extractSource(units, false) * J_PER_SOURCE;
    }

    private final class Journal extends SnapshotJournal<Integer> {
        @Override protected Integer createSnapshot() { return store.get().getSource(); }
        @Override protected void revertToSnapshot(Integer snapshot) { store.get().setSource(snapshot); }
    }
}
