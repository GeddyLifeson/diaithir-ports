package plus.dragons.createdragonsplus.compat;

import java.util.ArrayList;
import java.util.List;

/**
 * Minimal snapshot transaction in the shape of the Fabric Transfer API, so simulate-and-commit automation code
 * (arm interaction points, hatches) keeps its structure. Closing without {@link #commit()} rolls back every
 * {@link SnapshotParticipant} touched inside the transaction.
 */
public final class Transaction implements TransactionContext, AutoCloseable {
    private final List<Runnable> rollbacks = new ArrayList<>();
    private final List<Runnable> cleanups = new ArrayList<>();
    private final List<Runnable> commits = new ArrayList<>();
    private boolean closed;
    private boolean committed;

    private Transaction() {}

    public static Transaction openOuter() {
        return new Transaction();
    }

    @Override
    public void addRollback(Runnable rollback) {
        rollbacks.add(rollback);
    }

    @Override
    public void addCommitAction(Runnable action) {
        commits.add(action);
    }

    @Override
    public void addCleanup(Runnable cleanup) {
        cleanups.add(cleanup);
    }

    public void commit() {
        committed = true;
    }

    public void abort() {
        committed = false;
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }
        closed = true;
        if (committed) {
            commits.forEach(Runnable::run);
        } else {
            for (int i = rollbacks.size() - 1; i >= 0; i--) {
                rollbacks.get(i).run();
            }
        }
        cleanups.forEach(Runnable::run);
    }
}
