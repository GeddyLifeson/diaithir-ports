package com.mrh0.createaddition.energy.fe;

import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/** Legacy-style storage that is also a native EnergyHandler, so it can be handed to the capability system directly. */
public class EnergyStorage implements IEnergyStorage, EnergyHandler {
  protected int energy;
  protected int capacity;
  protected int maxReceive;
  protected int maxExtract;

  private final SnapshotJournal<Integer> journal = new SnapshotJournal<>() {
    @Override protected Integer createSnapshot() { return energy; }
    @Override protected void revertToSnapshot(Integer snapshot) { energy = snapshot; }
  };

  public EnergyStorage(int capacity) { this(capacity, capacity, capacity, 0); }
  public EnergyStorage(int capacity, int maxTransfer) { this(capacity, maxTransfer, maxTransfer, 0); }
  public EnergyStorage(int capacity, int maxReceive, int maxExtract) { this(capacity, maxReceive, maxExtract, 0); }
  public EnergyStorage(int capacity, int maxReceive, int maxExtract, int energy) {
    this.capacity = capacity;
    this.maxReceive = maxReceive;
    this.maxExtract = maxExtract;
    this.energy = Math.max(0, Math.min(capacity, energy));
  }

  @Override public int receiveEnergy(int max, boolean simulate) {
    if (!canReceive()) return 0;
    int r = Math.min(capacity - energy, Math.min(maxReceive, max));
    if (r > 0 && !simulate) energy += r;
    return Math.max(r, 0);
  }
  @Override public int extractEnergy(int max, boolean simulate) {
    if (!canExtract()) return 0;
    int e = Math.min(energy, Math.min(maxExtract, max));
    if (e > 0 && !simulate) energy -= e;
    return Math.max(e, 0);
  }
  @Override public int getEnergyStored() { return energy; }
  @Override public int getMaxEnergyStored() { return capacity; }
  @Override public boolean canExtract() { return maxExtract > 0; }
  @Override public boolean canReceive() { return maxReceive > 0; }

  // new API
  @Override public long getAmountAsLong() { return getEnergyStored(); }
  @Override public long getCapacityAsLong() { return getMaxEnergyStored(); }
  @Override public int insert(int amount, TransactionContext tx) {
    if (amount <= 0) return 0;
    int r = receiveEnergy(amount, true);
    if (r > 0) { journal.updateSnapshots(tx); receiveEnergy(r, false); }
    return r;
  }
  @Override public int extract(int amount, TransactionContext tx) {
    if (amount <= 0) return 0;
    int e = extractEnergy(amount, true);
    if (e > 0) { journal.updateSnapshots(tx); extractEnergy(e, false); }
    return e;
  }
}
