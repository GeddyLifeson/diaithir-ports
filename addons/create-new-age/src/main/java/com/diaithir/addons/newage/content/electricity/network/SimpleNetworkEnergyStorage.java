package com.diaithir.addons.newage.content.electricity.network;

import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import com.diaithir.addons.newage.content.electricity.connector.AbstractElectricalConnector;
import com.diaithir.addons.newage.esl.EnergyStorage;
import com.diaithir.addons.newage.esl.SnapshotParticipant;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.Map;

public class SimpleNetworkEnergyStorage extends NetworkEnergyStorage {
    private long capacity;
    private long stored = 0;
    private boolean supportsInsertion = true;
    private boolean supportsExtraction = true;
    private long maxExtract = Long.MAX_VALUE;
    private long maxInsert = Long.MAX_VALUE;

    private Runnable finalCommitCallback = () -> {};

    public SimpleNetworkEnergyStorage(AbstractElectricalConnector connector, ElectricalNetwork network, long capacity) {
        super(connector, network);
        if (capacity < 0) {
            throw new IllegalArgumentException("SimpleEnergyStorage capacity can't be negative");
        }

        this.capacity = capacity;
    }

    public SimpleNetworkEnergyStorage onFinalCommit(Runnable callback) {
        finalCommitCallback = callback;
        return this;
    }

    public SimpleNetworkEnergyStorage setCapacity(long capacity) {
        this.capacity = capacity;
        stored = Math.min(stored, capacity);

        return this;
    }

    public SimpleNetworkEnergyStorage setSupportsInsertion(boolean supportsInsertion) {
        this.supportsInsertion = supportsInsertion;
        return this;
    }

    public SimpleNetworkEnergyStorage setSupportsExtraction(boolean supportsExtraction) {
        this.supportsExtraction = supportsExtraction;
        return this;
    }

    public SimpleNetworkEnergyStorage setMaxExtract(long maxExtract) {
        this.maxExtract = maxExtract;
        return this;
    }

    public SimpleNetworkEnergyStorage setMaxInsert(long maxInsert) {
        this.maxInsert = maxInsert;
        return this;
    }

    public SimpleNetworkEnergyStorage setStoredEnergy(long amount) {
        stored = Math.max(0, Math.min(amount, capacity));
        return this;
    }

    @Override
    protected void onFinalCommit() {
        finalCommitCallback.run();
    }

    @Override
    public long getCapacity() {
        return capacity;
    }

    @Override
    public long getStoredEnergy() {
        return stored;
    }


    @Override
    public long insert(long amount, TransactionContext context) {
        if (!supportsInsertion) {
            return 0;
        }

        updateSnapshots(context);

        long inserted = Math.min(Math.min(amount, maxInsert), capacity - stored);
        stored += inserted;

        return inserted;
    }

    @Override
    public long extract(long amount, TransactionContext context) {
        if (!supportsExtraction) {
            return 0;
        }

        updateSnapshots(context);

        long extracted = Math.min(Math.min(amount, maxExtract), stored);
        stored -= extracted;

        return extracted;
    }

    public long internalInsert(long amount, boolean simulated) {
        long inserted = Math.min(amount, capacity - stored);

        if (simulated) {
            return inserted;
        }

        stored += inserted;
        return inserted;
    }

    public long internalExtract(long amount, boolean simulated) {
        long extracted = Math.min(amount, stored);

        if (simulated) {
            return extracted;
        }

        stored -= extracted;
        return extracted;
    }

    @Override
    public boolean supportsExtraction() {
        return supportsExtraction;
    }

    @Override
    public boolean supportsInsertion() {
        return supportsInsertion;
    }

}
