/*
 * This file is part of Industrial Foregoing.
 *
 * Copyright 2021, Buuz135
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in the
 * Software without restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to the
 * following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies
 * or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.buuz135.industrial.item.infinity;

import com.buuz135.industrial.gui.component.InfinityEnergyScreenAddon;
import com.hrznstudio.titanium.api.IFactory;
import com.hrznstudio.titanium.api.client.IScreenAddon;
import com.hrznstudio.titanium.component.IComponentHarness;
import com.hrznstudio.titanium.component.energy.EnergyStorageComponent;
import com.hrznstudio.titanium.container.addon.IContainerAddon;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

public class InfinityEnergyStorage<T extends IComponentHarness> extends EnergyStorageComponent<T> {

    private final long capacity;
    private long energy;

    public InfinityEnergyStorage(long capacity, int xPos, int yPos) {
        super(Integer.MAX_VALUE, xPos, yPos);
        this.energy = 0;
        this.capacity = capacity;
    }

    /** Titanium's energy component is transactional since 26.1; the stored value is a long, so it is journaled here. */
    private final Journal journal = new Journal();

    private final class Journal extends SnapshotJournal<Long> {
        @Override
        protected Long createSnapshot() {
            return getLongEnergyStored();
        }

        @Override
        protected void revertToSnapshot(Long snapshot) {
            setEnergyStored(snapshot);
        }

        void save(TransactionContext transaction) {
            updateSnapshots(transaction);
        }
    }

    @Override
    public int insert(int amount, TransactionContext transaction) {
        if (!canReceive()) return 0;
        long stored = getLongEnergyStored();
        int received = (int) Math.min(capacity - stored, amount);
        if (received <= 0) return 0;
        journal.save(transaction);
        setEnergyStored(stored + received);
        return received;
    }

    @Override
    public int extract(int amount, TransactionContext transaction) {
        return 0;
    }

    @Override
    public long getAmountAsLong() {
        return getLongEnergyStored();
    }

    @Override
    public long getCapacityAsLong() {
        return capacity;
    }

    @Override
    public int getEnergyStored() {
        return this.energy > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) this.energy;
    }

    public void setEnergyStored(long power) {
        this.energy = power;
        if (this.componentHarness != null) {
            this.componentHarness.markComponentForUpdate(false);
        }
    }

    @Override
    public int getMaxEnergyStored() {
        return (int) capacity;
    }

    public boolean canExtract() {
        return false;
    }

    public boolean canReceive() {
        return true;
    }

    public long getLongEnergyStored() {
        return this.energy;
    }

    public long getLongCapacity() {
        return capacity;
    }

    @Override
    @Nonnull
    public List<IFactory<? extends IContainerAddon>> getContainerAddons() {
        return Collections.emptyList();
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag nbt = new CompoundTag();
        nbt.putLong("energy", this.energy);
        return nbt;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        this.energy = nbt.getLongOr("energy", 0L);
    }

    @Nonnull
    @Override
    @OnlyIn(Dist.CLIENT)
    public List<IFactory<?>> getScreenAddons() {
        return Collections.singletonList(() -> new InfinityEnergyScreenAddon(getX(), getY(), this));
    }
}
