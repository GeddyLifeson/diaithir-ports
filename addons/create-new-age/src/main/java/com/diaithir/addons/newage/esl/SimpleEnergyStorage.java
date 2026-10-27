package com.diaithir.addons.newage.esl;

import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class SimpleEnergyStorage extends SnapshotParticipant<Long> implements EnergyStorage {
    private long capacity;
    private long stored;
    private boolean supportsInsertion = true;
    private boolean supportsExtraction = true;
    private long maxExtract = Long.MAX_VALUE;
    private long maxInsert = Long.MAX_VALUE;
    private Runnable finalCommitCallback = () -> {};

    public SimpleEnergyStorage(long capacity) { this.capacity = capacity; }

    public SimpleEnergyStorage onFinalCommit(Runnable r) { finalCommitCallback = r; return this; }
    public SimpleEnergyStorage setCapacity(long c) { capacity = c; return this; }
    public SimpleEnergyStorage setSupportsInsertion(boolean b) { supportsInsertion = b; return this; }
    public SimpleEnergyStorage setSupportsExtraction(boolean b) { supportsExtraction = b; return this; }
    public SimpleEnergyStorage setMaxExtract(long v) { maxExtract = v; return this; }
    public SimpleEnergyStorage setMaxInsert(long v) { maxInsert = v; return this; }
    public SimpleEnergyStorage setStoredEnergy(long v) { stored = Math.max(0, Math.min(capacity, v)); return this; }

    @Override protected void onFinalCommit() { finalCommitCallback.run(); }
    @Override public long getCapacity() { return capacity; }
    @Override public long getStoredEnergy() { return stored; }
    @Override public Long createSnapshot() { return stored; }
    @Override public void restoreSnapshot(Long s) { stored = s; }
    @Override public boolean supportsExtraction() { return supportsExtraction; }
    @Override public boolean supportsInsertion() { return supportsInsertion; }

    @Override
    public long insert(long amount, TransactionContext tx) {
        if (!supportsInsertion) return 0;
        long n = Math.min(Math.min(amount, maxInsert), capacity - stored);
        if (n <= 0) return 0;
        updateSnapshots(tx);
        stored += n;
        return n;
    }

    @Override
    public long extract(long amount, TransactionContext tx) {
        if (!supportsExtraction) return 0;
        long n = Math.min(Math.min(amount, maxExtract), stored);
        if (n <= 0) return 0;
        updateSnapshots(tx);
        stored -= n;
        return n;
    }

    public long internalInsert(long amount, boolean simulate) {
        long n = Math.max(0, Math.min(amount, capacity - stored));
        if (!simulate) stored += n;
        return n;
    }

    public long internalExtract(long amount, boolean simulate) {
        long n = Math.max(0, Math.min(amount, stored));
        if (!simulate) stored -= n;
        return n;
    }
}
