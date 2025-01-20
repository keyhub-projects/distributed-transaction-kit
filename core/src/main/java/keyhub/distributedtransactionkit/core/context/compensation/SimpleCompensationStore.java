package keyhub.distributedtransactionkit.core.context.compensation;

import keyhub.distributedtransactionkit.core.transaction.KhTransaction;
import keyhub.distributedtransactionkit.core.transaction.TransactionId;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SimpleCompensationStore implements CompensationStore {
    private final Map<TransactionId, KhTransaction> compensations;

    public SimpleCompensationStore() {
        this.compensations = new ConcurrentHashMap<>();
    }

    @Override
    public void add(KhTransaction compensationTransaction) {

    }

    @Override
    public void compensate() {

    }

    @Override
    public void clear() {

    }
}
