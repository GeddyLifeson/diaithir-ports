package plus.dragons.createdragonsplus.compat;

public interface TransactionContext {
    /** Runs (in reverse order) if the transaction closes without being committed. */
    void addRollback(Runnable rollback);

    /** Runs on close only if the transaction was committed. */
    void addCommitAction(Runnable action);

    /** Runs when the transaction closes, committed or not. */
    void addCleanup(Runnable cleanup);
}
