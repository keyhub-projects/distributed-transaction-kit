package keyhub.distributedtransactionkit.core.context.outbox;

import keyhub.distributedtransactionkit.core.transaction.KhTransaction;

public interface OutboxStore {

    void add(KhTransaction outboxTransaction);
}
