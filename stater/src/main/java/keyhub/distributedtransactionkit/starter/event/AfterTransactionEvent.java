package keyhub.distributedtransactionkit.starter.event;

import keyhub.distributedtransactionkit.core.transaction.KhTransaction;

public class AfterTransactionEvent {
    private final KhTransaction transaction;

    public AfterTransactionEvent(KhTransaction transaction) {
        this.transaction = transaction;
    }

    public KhTransaction getTransaction() {
        return transaction;
    }
}
