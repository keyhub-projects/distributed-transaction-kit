package keyhub.distributedtransactionkit.core.context.compensation;

import keyhub.distributedtransactionkit.core.transaction.KhTransaction;
import keyhub.distributedtransactionkit.core.transaction.TransactionId;

import java.util.List;

public interface CompensationStore {
    static CompensationStore of() {
        return SimpleCompensationStore.of();
    }

    void add(TransactionId transactionId, KhTransaction compensation);

    KhTransaction pop(TransactionId transactionId);

    List<KhTransaction> popAll();
}
