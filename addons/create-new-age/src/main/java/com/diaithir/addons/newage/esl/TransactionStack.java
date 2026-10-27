package com.diaithir.addons.newage.esl;

import net.neoforged.neoforge.transfer.transaction.Transaction;

/** Compatibility facade over NeoForge's transaction API. */
public final class TransactionStack {
    private static final TransactionStack INSTANCE = new TransactionStack();
    private TransactionStack() {}
    public static TransactionStack get() { return INSTANCE; }
    public Transaction openOuter() { return Transaction.openRoot(); }
}
