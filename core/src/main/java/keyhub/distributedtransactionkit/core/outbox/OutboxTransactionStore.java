package keyhub.distributedtransactionkit.core.outbox;

import keyhub.distributedtransactionkit.core.transaction.KhTransaction;

public interface OutboxTransactionStore {

    void add(KhTransaction outboxTransaction);
}
