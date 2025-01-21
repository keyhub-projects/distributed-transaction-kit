package keyhub.distributedtransactionkit.core.context.compensation;

import keyhub.distributedtransactionkit.core.transaction.KhTransaction;
import keyhub.distributedtransactionkit.core.transaction.TransactionId;

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SimpleCompensationStore implements CompensationStore {

    private final Queue<TransactionId> transactionIdQueue = new ConcurrentLinkedQueue<>();
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
    public void add(TransactionId transactionId, KhTransaction compensation) {
        transactionIdQueue.add(transactionId);
        compensations.putIfAbsent(transactionId, compensation);
    }

    @Override
    public KhTransaction pop(TransactionId transactionId) {
        transactionIdQueue.remove(transactionId);
        return compensations.remove(transactionId);
    }

    @Override
    public List<KhTransaction> popAll() {
        return transactionIdQueue.stream()
                .map(this::pop)
                .toList();
    }
}
