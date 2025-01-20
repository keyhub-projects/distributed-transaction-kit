package keyhub.distributedtransactionkit.core.context.outbox;

import keyhub.distributedtransactionkit.core.transaction.KhTransaction;
import keyhub.distributedtransactionkit.core.transaction.TransactionId;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SimpleOutboxStore implements OutboxStore {

    private final Map<TransactionId, KhTransaction> outboxes;

    SimpleOutboxStore(Map<TransactionId, KhTransaction> outboxes) {
        this.outboxes = outboxes;
    }

    static SimpleOutboxStore of(){
        return new SimpleOutboxStore(
                new ConcurrentHashMap<>()
        );
    }

    @Override
    public void add(KhTransaction outboxTransaction) {
        outboxes.put(outboxTransaction.getTransactionId(), outboxTransaction);
    }

    @Override
    public KhTransaction poll(TransactionId transactionId) {
        return outboxes.remove(transactionId);
    }
}
