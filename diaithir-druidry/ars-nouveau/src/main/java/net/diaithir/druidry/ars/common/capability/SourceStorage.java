package net.diaithir.druidry.ars.common.capability;

import net.diaithir.druidry.ars.api.source.ISourceCap;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.common.util.ValueIOSerializable;

public class SourceStorage implements ISourceCap, ValueIOSerializable {
    protected int source;
    protected int capacity;
    protected int maxReceive;
    protected int maxExtract;

    /**
     * Constructor for creating a new empty source storage, with maximum transfer rate.
     *
     * @param capacity The maximum amount of source that can be stored.
     */
    public SourceStorage(int capacity) {
        this(capacity, capacity, capacity, 0);
    }

    /**
     * Constructor for creating a new empty source storage, with specific transfer rates.
     *
     * @param capacity    The maximum amount of source that can be stored.
     * @param maxTransfer The maximum amount of source that can be transferred per operation.
     */
    public SourceStorage(int capacity, int maxTransfer) {
        this(capacity, maxTransfer, maxTransfer, 0);
    }

    /**
     * Constructor for creating a new empty source storage, with specific in/out transfer rates.
     *
     * @param capacity   The maximum amount of source that can be stored.
     * @param maxReceive The maximum amount of source that can be received per operation.
     * @param maxExtract The maximum amount of source that can be extracted per operation.
     */
    public SourceStorage(int capacity, int maxReceive, int maxExtract) {
        this(capacity, maxReceive, maxExtract, 0);
    }

    /**
     * Constructor for creating a new source storage, use to initialize with a specific amount of source.
     *
     * @param capacity   The maximum amount of source that can be stored.
     * @param maxReceive The maximum amount of source that can be received per operation.
     * @param maxExtract The maximum amount of source that can be extracted per operation.
     * @param source     The initial amount of source to create the storage with.
     */
    public SourceStorage(int capacity, int maxReceive, int maxExtract, int source) {
        this.capacity = capacity;
        this.maxReceive = maxReceive;
        this.maxExtract = maxExtract;
        this.source = Math.max(0, Math.min(capacity, source));
    }

    /**
     * Dynamically adjust the maximum amount of source that can be extracted per operation.
     *
     * @param maxExtract The maximum amount of source that can be extracted per operation.
     */
    public void setMaxExtract(int maxExtract) {
        this.maxExtract = maxExtract;
    }

    /**
     * Dynamically adjust the maximum amount of source that can be received per operation.
     *
     * @param maxReceive The maximum amount of source that can be received per operation.
     */
    public void setMaxReceive(int maxReceive) {
        this.maxReceive = maxReceive;
    }

    public int getMaxExtract() {
        return maxExtract;
    }

    public int getMaxReceive() {
        return maxReceive;
    }

    @Override
    public void setSource(int source) {
        this.source = Math.clamp(source, 0, this.capacity);
    }

    /**
     * Adds source to the tile, can be simulated to check the max amount that can be received.
     *
     * @param toReceive The amount of source to receive.
     * @param simulate  If the action should be simulated.
     * @return The amount of source received.
     * Calls onContentsChanged() if source is received.
     */
    @Override
    public int receiveSource(int toReceive, boolean simulate) {
        if (!canReceive() || toReceive <= 0) {
            return 0;
        }

        int sourceReceived = Mth.clamp(this.capacity - this.source, 0, Math.min(this.maxReceive, toReceive));
        if (!simulate) {
            this.source += sourceReceived;
            onContentsChanged();
        }
        return sourceReceived;
    }


    /**
     * Removes source from the tile, can be simulated to check the max amount that can be extracted.
     *
     * @param toExtract The amount of source to extract.
     * @param simulate  If the action should be simulated.
     * @return The amount of source extracted.
     * Calls onContentsChanged() if source is extracted.
     */
    @Override
    public int extractSource(int toExtract, boolean simulate) {
        if (!canExtract() || toExtract <= 0) {
            return 0;
        }

        int sourceExtracted = Math.min(this.source, Math.min(this.maxExtract, toExtract));
        if (!simulate) {
            this.source -= sourceExtracted;
            onContentsChanged();
        }

        return sourceExtracted;
    }

    @Override
    public int getSource() {
        return this.source;
    }

    @Override
    public void setMaxSource(int max) {
        this.capacity = max;
    }

    @Override
    public int getSourceCapacity() {
        return this.capacity;
    }

    /*
     * Checks if the tile can accept any source
     */
    @Override
    public boolean canAcceptSource(int source) {
        return receiveSource(source, true) > 0;
    }

    @Override
    public boolean canProvideSource(int source) {
        return extractSource(source, true) > 0;
    }

    @Override
    public boolean canReceive() {
        return this.maxReceive > 0;
    }

    @Override
    public boolean canExtract() {
        return this.maxExtract > 0;
    }

    @Override
    public void serialize(net.minecraft.world.level.storage.ValueOutput output) {
        output.putInt("source", this.getSource());
    }

    @Override
    public void deserialize(net.minecraft.world.level.storage.ValueInput input) {
        this.source = input.getIntOr("source", 0);
    }

    /**
     * Called when the contents of this storage have changed
     */
    public void onContentsChanged() {
    }

}