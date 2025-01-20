package keyhub.distributedtransactionkit.core.context.compensation;

import keyhub.distributedtransactionkit.core.transaction.KhTransaction;
import keyhub.distributedtransactionkit.core.transaction.TransactionId;

public interface CompensationStore {
    static CompensationStore of() {
        return SimpleCompensationStore.of();
    }

    void add(KhTransaction compensation);

    KhTransaction pop(TransactionId transactionId);
}
