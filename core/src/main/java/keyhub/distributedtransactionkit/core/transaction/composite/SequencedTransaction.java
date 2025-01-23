package keyhub.distributedtransactionkit.core.transaction.composite;

import keyhub.distributedtransactionkit.core.context.KhTransactionContext;

public interface SequencedTransaction extends CompositeTransaction {

    static SequencedTransaction from(KhTransactionContext transactionContext) {
        return new SimpleSequencedTransaction(transactionContext);
    }
}
