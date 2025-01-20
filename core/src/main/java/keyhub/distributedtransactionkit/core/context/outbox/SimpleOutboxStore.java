package keyhub.distributedtransactionkit.core.context.outbox;

import keyhub.distributedtransactionkit.core.transaction.KhTransaction;
import keyhub.distributedtransactionkit.core.transaction.TransactionId;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SimpleOutboxStore implements OutboxStore {

    private final Map<TransactionId, KhTransaction> outboxs;

    public SimpleOutboxStore() {
        this.outboxs = new ConcurrentHashMap<>();
    }

    @Override
    public void add(KhTransaction outboxTransaction) {

    }
}
