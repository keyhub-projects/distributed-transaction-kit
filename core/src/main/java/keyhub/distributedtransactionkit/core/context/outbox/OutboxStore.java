package keyhub.distributedtransactionkit.core.context.outbox;

import keyhub.distributedtransactionkit.core.transaction.KhTransaction;
import keyhub.distributedtransactionkit.core.transaction.TransactionId;

import java.util.List;

public interface OutboxStore {

    static OutboxStore of() {
        return SimpleOutboxStore.of();
    }

    void add(TransactionId transactionId, KhTransaction outbox);

    KhTransaction poll(TransactionId transactionId);

    List<KhTransaction> pollAll();
}
