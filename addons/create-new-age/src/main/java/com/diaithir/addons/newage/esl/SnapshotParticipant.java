package com.diaithir.addons.newage.esl;

import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public abstract class SnapshotParticipant<T> {
    private final SnapshotJournal<T> journal = new SnapshotJournal<>() {
        @Override protected T createSnapshot() { return SnapshotParticipant.this.createSnapshot(); }
        @Override protected void revertToSnapshot(T snapshot) { SnapshotParticipant.this.restoreSnapshot(snapshot); }
        @Override protected void onRootCommit(T original) { SnapshotParticipant.this.onFinalCommit(); }
    };

    public abstract T createSnapshot();

    public abstract void restoreSnapshot(T snapshot);

    protected void onFinalCommit() {}

    protected void updateSnapshots(TransactionContext tx) { journal.updateSnapshots(tx); }
}
