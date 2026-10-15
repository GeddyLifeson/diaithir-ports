/*
 * This file ("CustomEnergyStorage.java") is part of the Actually Additions mod for Minecraft.
 * It is created and owned by Ellpeck and distributed
 * under the Actually Additions License to be found at
 * http://ellpeck.de/actaddlicense
 * View the source code at https://github.com/Ellpeck/ActuallyAdditions
 *
 * © 2015-2017 Ellpeck
 */

package de.ellpeck.actuallyadditions.mod.tile;

import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.transfer.energy.SimpleEnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;

/**
 * Built on NeoForge's transactional energy handler (the capability type since 26.1); the receive/extract methods with a
 * simulate flag are kept for Actually Additions' own machine code.
 */
public class CustomEnergyStorage extends SimpleEnergyHandler {
    boolean dirty = false;

    public CustomEnergyStorage(int capacity, int maxReceive, int maxExtract) {
        super(capacity, maxReceive, maxExtract);
    }

    @Override
    protected void onEnergyChanged(int previousAmount) {
        dirty = true;
    }

    public int receiveEnergy(int maxReceive, boolean simulate) {
        try (Transaction transaction = Transaction.openRoot()) {
            int received = insert(maxReceive, transaction);
            if (!simulate) transaction.commit();
            return received;
        }
    }

    public int extractEnergy(int maxExtract, boolean simulate) {
        try (Transaction transaction = Transaction.openRoot()) {
            int extracted = extract(maxExtract, transaction);
            if (!simulate) transaction.commit();
            return extracted;
        }
    }

    public int extractEnergyInternal(int maxExtract, boolean simulate) {
        int before = this.maxExtract;
        this.maxExtract = Integer.MAX_VALUE;

        int toReturn = this.extractEnergy(maxExtract, simulate);

        this.maxExtract = before;
        return toReturn;
    }

    public int receiveEnergyInternal(int maxReceive, boolean simulate) {
        int before = this.maxInsert;
        this.maxInsert = Integer.MAX_VALUE;

        int toReturn = this.receiveEnergy(maxReceive, simulate);

        this.maxInsert = before;
        return toReturn;
    }

    public int getEnergyStored() {
        return getAmountAsInt();
    }

    public int getMaxEnergyStored() {
        return getCapacityAsInt();
    }

    public boolean isDirty() {
        return dirty;
    }

    public void clearDirty() {
        dirty = false;
    }

    public void readFromNBT(CompoundTag compound) {
        if (compound.contains("Energy"))
            this.setEnergyStored(compound.getIntOr("Energy", 0));
    }

    public void writeToNBT(CompoundTag compound) {
        compound.putInt("Energy", this.getEnergyStored());
    }

    public void setEnergyStored(int energy) {
        set(energy);
    }
}
