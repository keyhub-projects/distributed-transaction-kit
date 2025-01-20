package keyhub.distributedtransactionkit.core.transaction;

import keyhub.distributedtransactionkit.core.context.compensation.CompensationStore;
import keyhub.distributedtransactionkit.core.lib.ApplicationContextProvider;
import keyhub.distributedtransactionkit.core.context.outbox.OutboxStore;

import lombok.Getter;

public abstract class AbstractTransaction implements KhTransaction {
    @Getter
    protected final TransactionId transactionId;
    protected final CompensationStore compensatingTransactionStore;
    protected final OutboxStore outboxTransactionStore;

    protected AbstractTransaction(CompensationStore compensatingTransactionStore, OutboxStore outboxTransactionStore) {
        this.transactionId = TransactionId.ofUuid();
        this.compensatingTransactionStore = compensatingTransactionStore;
        this.outboxTransactionStore = outboxTransactionStore;
    }

    protected AbstractTransaction(){
        this.transactionId = TransactionId.ofUuid();
        this.compensatingTransactionStore = ApplicationContextProvider.getApplicationContext().getBean(CompensationStore.class);
        this.outboxTransactionStore = ApplicationContextProvider.getApplicationContext().getBean(OutboxStore.class);
    }

    protected abstract void storeCompensation();

    protected abstract void storeOutbox();
}
