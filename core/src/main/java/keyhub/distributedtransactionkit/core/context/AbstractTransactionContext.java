package keyhub.distributedtransactionkit.core.context;

import keyhub.distributedtransactionkit.core.context.compensation.CompensationStore;
import keyhub.distributedtransactionkit.core.context.outbox.OutboxStore;
import keyhub.distributedtransactionkit.core.transaction.KhTransaction;
import keyhub.distributedtransactionkit.core.transaction.TransactionId;

public abstract class AbstractTransactionContext implements KhTransactionContext {
    protected final CompensationStore compensationStore;
    protected final OutboxStore outboxStore;

    public AbstractTransactionContext() {
        this.compensationStore = CompensationStore.of();
        this.outboxStore = OutboxStore.of();
    }

    @Override
    public void storeCompensation(TransactionId transactionId, KhTransaction compensation) {
        this.compensationStore.add(transactionId, compensation);
    }

    @Override
    public void storeOutbox(TransactionId transactionId, KhTransaction outbox) {
        this.outboxStore.add(transactionId, outbox);
    }
}
