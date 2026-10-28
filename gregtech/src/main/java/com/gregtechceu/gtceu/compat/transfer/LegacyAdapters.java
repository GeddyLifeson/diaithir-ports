package com.gregtechceu.gtceu.compat.transfer;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * Adapters from the 1.21-style IItemHandler / IFluidHandler / IEnergyStorage interfaces, which this mod still uses
 * internally, to the transaction based transfer API of NeoForge 26.1 (used for capabilities exposed to other mods).
 * <p>
 * Changes are applied immediately and rolled back from a snapshot if the transaction is aborted.
 */
public final class LegacyAdapters {
    private LegacyAdapters() {}

    public static ResourceHandler<ItemResource> item(IItemHandler handler) {
        return new Item(handler);
    }

    public static ResourceHandler<FluidResource> fluid(IFluidHandler handler) {
        return new Fluid(handler);
    }

    public static EnergyHandler energy(IEnergyStorage storage) {
        return new Energy(storage);
    }

    private static class Item implements ResourceHandler<ItemResource> {
        private final IItemHandler h;
        private final SnapshotJournal<ItemStack[]> journal = new SnapshotJournal<>() {
            @Override
            protected ItemStack[] createSnapshot() {
                ItemStack[] s = new ItemStack[h.getSlots()];
                for (int i = 0; i < s.length; i++) s[i] = h.getStackInSlot(i).copy();
                return s;
            }

            @Override
            protected void revertToSnapshot(ItemStack[] snapshot) {
                if (h instanceof IItemHandlerModifiable mod) {
                    for (int i = 0; i < snapshot.length; i++) mod.setStackInSlot(i, snapshot[i]);
                }
            }
        };

        Item(IItemHandler h) {
            this.h = h;
        }

        @Override
        public int size() {
            return h.getSlots();
        }

        @Override
        public ItemResource getResource(int index) {
            return ItemResource.of(h.getStackInSlot(index));
        }

        @Override
        public long getAmountAsLong(int index) {
            return h.getStackInSlot(index).getCount();
        }

        @Override
        public long getCapacityAsLong(int index, ItemResource resource) {
            return resource.isEmpty() ? h.getSlotLimit(index) : Math.min(h.getSlotLimit(index), resource.getMaxStackSize());
        }

        @Override
        public boolean isValid(int index, ItemResource resource) {
            return h.isItemValid(index, resource.toStack(1));
        }

        @Override
        public int insert(int index, ItemResource resource, int amount, TransactionContext tx) {
            if (resource.isEmpty() || amount <= 0) return 0;
            ItemStack stack = resource.toStack(amount);
            int inserted = amount - h.insertItem(index, stack, true).getCount();
            if (inserted > 0) {
                journal.updateSnapshots(tx);
                h.insertItem(index, stack.copyWithCount(inserted), false);
            }
            return inserted;
        }

        @Override
        public int extract(int index, ItemResource resource, int amount, TransactionContext tx) {
            if (resource.isEmpty() || amount <= 0) return 0;
            if (!resource.matches(h.getStackInSlot(index))) return 0;
            int extracted = h.extractItem(index, amount, true).getCount();
            if (extracted > 0) {
                journal.updateSnapshots(tx);
                return h.extractItem(index, extracted, false).getCount();
            }
            return 0;
        }
    }

    private static class Fluid implements ResourceHandler<FluidResource> {
        private final IFluidHandler h;
        private final SnapshotJournal<FluidStack[]> journal = new SnapshotJournal<>() {
            @Override
            protected FluidStack[] createSnapshot() {
                FluidStack[] s = new FluidStack[h.getTanks()];
                for (int i = 0; i < s.length; i++) s[i] = h.getFluidInTank(i).copy();
                return s;
            }

            @Override
            protected void revertToSnapshot(FluidStack[] snapshot) {
                if (h instanceof FluidTank tank && snapshot.length == 1) {
                    tank.setFluid(snapshot[0]);
                } else {
                    // generic best effort: drain everything, then refill what we had
                    for (int i = 0; i < snapshot.length; i++) {
                        FluidStack cur = h.getFluidInTank(i);
                        if (!cur.isEmpty()) h.drain(cur.copy(), IFluidHandler.FluidAction.EXECUTE);
                    }
                    for (FluidStack fs : snapshot) {
                        if (!fs.isEmpty()) h.fill(fs.copy(), IFluidHandler.FluidAction.EXECUTE);
                    }
                }
            }
        };

        Fluid(IFluidHandler h) {
            this.h = h;
        }

        @Override
        public int size() {
            return h.getTanks();
        }

        @Override
        public FluidResource getResource(int index) {
            return FluidResource.of(h.getFluidInTank(index));
        }

        @Override
        public long getAmountAsLong(int index) {
            return h.getFluidInTank(index).getAmount();
        }

        @Override
        public long getCapacityAsLong(int index, FluidResource resource) {
            return h.getTankCapacity(index);
        }

        @Override
        public boolean isValid(int index, FluidResource resource) {
            return h.isFluidValid(index, resource.toStack(1));
        }

        @Override
        public int insert(int index, FluidResource resource, int amount, TransactionContext tx) {
            if (resource.isEmpty() || amount <= 0) return 0;
            if (h.getTanks() > 1 && !h.getFluidInTank(index).isEmpty() && !resource.matches(h.getFluidInTank(index))) return 0;
            return doFill(resource, amount, tx);
        }

        @Override
        public int insert(FluidResource resource, int amount, TransactionContext tx) {
            if (resource.isEmpty() || amount <= 0) return 0;
            return doFill(resource, amount, tx);
        }

        private int doFill(FluidResource resource, int amount, TransactionContext tx) {
            FluidStack fs = resource.toStack(amount);
            int filled = h.fill(fs, IFluidHandler.FluidAction.SIMULATE);
            if (filled > 0) {
                journal.updateSnapshots(tx);
                return h.fill(fs.copyWithAmount(filled), IFluidHandler.FluidAction.EXECUTE);
            }
            return 0;
        }

        @Override
        public int extract(int index, FluidResource resource, int amount, TransactionContext tx) {
            if (resource.isEmpty() || amount <= 0) return 0;
            if (!resource.matches(h.getFluidInTank(index))) return 0;
            return doDrain(resource, amount, tx);
        }

        @Override
        public int extract(FluidResource resource, int amount, TransactionContext tx) {
            if (resource.isEmpty() || amount <= 0) return 0;
            return doDrain(resource, amount, tx);
        }

        private int doDrain(FluidResource resource, int amount, TransactionContext tx) {
            FluidStack fs = resource.toStack(amount);
            int drained = h.drain(fs, IFluidHandler.FluidAction.SIMULATE).getAmount();
            if (drained > 0) {
                journal.updateSnapshots(tx);
                return h.drain(fs.copyWithAmount(drained), IFluidHandler.FluidAction.EXECUTE).getAmount();
            }
            return 0;
        }
    }

    private static class Energy implements EnergyHandler {
        private final IEnergyStorage s;
        private final SnapshotJournal<Integer> journal = new SnapshotJournal<>() {
            @Override
            protected Integer createSnapshot() {
                return s.getEnergyStored();
            }

            @Override
            protected void revertToSnapshot(Integer snapshot) {
                int cur = s.getEnergyStored();
                if (cur > snapshot) s.extractEnergy(cur - snapshot, false);
                else if (cur < snapshot) s.receiveEnergy(snapshot - cur, false);
            }
        };

        Energy(IEnergyStorage s) {
            this.s = s;
        }

        @Override
        public long getAmountAsLong() {
            return s.getEnergyStored();
        }

        @Override
        public long getCapacityAsLong() {
            return s.getMaxEnergyStored();
        }

        @Override
        public int insert(int amount, TransactionContext tx) {
            int n = s.receiveEnergy(amount, true);
            if (n > 0) {
                journal.updateSnapshots(tx);
                return s.receiveEnergy(n, false);
            }
            return 0;
        }

        @Override
        public int extract(int amount, TransactionContext tx) {
            int n = s.extractEnergy(amount, true);
            if (n > 0) {
                journal.updateSnapshots(tx);
                return s.extractEnergy(n, false);
            }
            return 0;
        }
    }
}
