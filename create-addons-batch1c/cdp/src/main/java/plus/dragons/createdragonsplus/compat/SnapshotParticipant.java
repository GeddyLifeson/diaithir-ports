package plus.dragons.createdragonsplus.compat;

/** Snapshot/rollback helper matching the Fabric Transfer API base class. */
public abstract class SnapshotParticipant<T> {
    private final java.util.IdentityHashMap<TransactionContext, Boolean> seen = new java.util.IdentityHashMap<>();

    protected abstract T createSnapshot();

    protected abstract void readSnapshot(T snapshot);

    protected void onFinalCommit() {}

    public void updateSnapshots(TransactionContext transaction) {
        if (seen.put(transaction, Boolean.TRUE) != null) {
            return;
        }
        T snapshot = createSnapshot();
        transaction.addRollback(() -> readSnapshot(snapshot));
        transaction.addCommitAction(this::onFinalCommit);
        transaction.addCleanup(() -> seen.remove(transaction));
    }
}
