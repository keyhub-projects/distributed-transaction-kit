package keyhub.distributedtransactionkit.core.context.outbox;

import keyhub.distributedtransactionkit.core.transaction.KhTransaction;
import keyhub.distributedtransactionkit.core.transaction.TransactionId;

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SimpleOutboxStore implements OutboxStore {

    private final Queue<TransactionId> transactionIdQueue = new ConcurrentLinkedQueue<>();
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
    public void add(TransactionId transactionId, KhTransaction outbox) {
        transactionIdQueue.add(transactionId);
        outboxes.put(transactionId, outbox);
    }

    @Override
    public KhTransaction poll(TransactionId transactionId) {
        transactionIdQueue.remove(transactionId);
        return outboxes.remove(transactionId);
    }

    @Override
    public List<KhTransaction> pollAll() {
        return transactionIdQueue.stream()
                .map(this::poll)
                .toList();
    }
}
