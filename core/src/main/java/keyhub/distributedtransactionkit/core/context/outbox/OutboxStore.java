package keyhub.distributedtransactionkit.core.context.outbox;

import keyhub.distributedtransactionkit.core.transaction.KhTransaction;
import keyhub.distributedtransactionkit.core.transaction.TransactionId;

public interface OutboxStore {

    static OutboxStore of() {
        return SimpleOutboxStore.of();
    }

    void add(KhTransaction outboxTransaction);

    KhTransaction poll(TransactionId transactionId);
}
