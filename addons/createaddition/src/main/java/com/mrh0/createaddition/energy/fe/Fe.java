package com.mrh0.createaddition.energy.fe;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;

/** Bridges between the legacy IEnergyStorage view and the transactional EnergyHandler capability. */
public final class Fe {
  private Fe() {}

  public static @Nullable IEnergyStorage block(Level level, BlockPos pos, @Nullable Direction side) {
    return wrap(level.getCapability(Capabilities.Energy.BLOCK, pos, side));
  }

  public static @Nullable IEnergyStorage item(net.minecraft.world.item.ItemStack stack) {
    return wrap(stack.getCapability(Capabilities.Energy.ITEM, net.neoforged.neoforge.transfer.access.ItemAccess.forStack(stack)));
  }

  public static @Nullable EnergyHandler asHandler(@Nullable IEnergyStorage s) {
    if (s == null) return null;
    if (s instanceof EnergyHandler h) return h;
    return new EnergyHandler() {
      @Override public long getAmountAsLong() { return s.getEnergyStored(); }
      @Override public long getCapacityAsLong() { return s.getMaxEnergyStored(); }
      @Override public int insert(int amount, net.neoforged.neoforge.transfer.transaction.TransactionContext tx) { return s.receiveEnergy(amount, false); }
      @Override public int extract(int amount, net.neoforged.neoforge.transfer.transaction.TransactionContext tx) { return s.extractEnergy(amount, false); }
    };
  }

  public static @Nullable IEnergyStorage wrap(@Nullable EnergyHandler h) {
    if (h == null) return null;
    if (h instanceof IEnergyStorage ies) return ies;
    return new IEnergyStorage() {
      @Override public int receiveEnergy(int max, boolean simulate) {
        try (Transaction tx = Transaction.openRoot()) {
          int r = h.insert(max, tx);
          if (!simulate) tx.commit();
          return r;
        }
      }
      @Override public int extractEnergy(int max, boolean simulate) {
        try (Transaction tx = Transaction.openRoot()) {
          int r = h.extract(max, tx);
          if (!simulate) tx.commit();
          return r;
        }
      }
      @Override public int getEnergyStored() { return h.getAmountAsInt(); }
      @Override public int getMaxEnergyStored() { return h.getCapacityAsInt(); }
      @Override public boolean canExtract() { try (Transaction tx = Transaction.openRoot()) { return h.extract(1, tx) > 0; } }
      @Override public boolean canReceive() { try (Transaction tx = Transaction.openRoot()) { return h.insert(1, tx) > 0; } }
    };
  }
}
