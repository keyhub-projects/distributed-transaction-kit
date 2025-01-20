package keyhub.distributedtransactionkit.core.context.compensation;

import keyhub.distributedtransactionkit.core.transaction.KhTransaction;
import keyhub.distributedtransactionkit.core.transaction.TransactionId;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SimpleCompensationStore implements CompensationStore {
    private final Map<TransactionId, KhTransaction> compensations;

    SimpleCompensationStore(Map<TransactionId, KhTransaction> compensations) {
        this.compensations = compensations;
    }

    static SimpleCompensationStore of() {
        return new SimpleCompensationStore(
                new ConcurrentHashMap<>()
        );
    }

    @Override
    public void add(KhTransaction compensation) {
        compensations.putIfAbsent(compensation.getTransactionId(), compensation);
    }

    @Override
    public KhTransaction pop(TransactionId transactionId) {
        return compensations.remove(transactionId);
    }
}
