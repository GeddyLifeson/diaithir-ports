package sonar.fluxnetworks.common.util;

import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * Exposes a pre-26.1 energy storage as the 26.1 energy capability. It cannot be rolled
 * back, so a transfer is simulated when asked and carried out when the outermost transaction commits.
 */
public class LegacyEnergyHandler extends SnapshotJournal<int[]> implements EnergyHandler {
    private final IEnergyStorage storage;
    private int queuedIn;
    private int queuedOut;

    public LegacyEnergyHandler(IEnergyStorage storage) {
        this.storage = storage;
    }

    @Override
    public long getAmountAsLong() {
        return storage.getEnergyStored();
    }

    @Override
    public long getCapacityAsLong() {
        return storage.getMaxEnergyStored();
    }

    @Override
    public int insert(int amount, TransactionContext transaction) {
        int accepted = storage.receiveEnergy(amount + queuedIn, true) - queuedIn;
        if (accepted <= 0) return 0;
        updateSnapshots(transaction);
        queuedIn += Math.min(amount, accepted);
        return Math.min(amount, accepted);
    }

    @Override
    public int extract(int amount, TransactionContext transaction) {
        int available = storage.extractEnergy(amount + queuedOut, true) - queuedOut;
        if (available <= 0) return 0;
        updateSnapshots(transaction);
        queuedOut += Math.min(amount, available);
        return Math.min(amount, available);
    }

    @Override
    protected int[] createSnapshot() {
        return new int[] {queuedIn, queuedOut};
    }

    @Override
    protected void revertToSnapshot(int[] snapshot) {
        queuedIn = snapshot[0];
        queuedOut = snapshot[1];
    }

    @Override
    protected void onRootCommit(int[] originalState) {
        if (queuedIn > 0) storage.receiveEnergy(queuedIn, false);
        if (queuedOut > 0) storage.extractEnergy(queuedOut, false);
        queuedIn = 0;
        queuedOut = 0;
    }
}
