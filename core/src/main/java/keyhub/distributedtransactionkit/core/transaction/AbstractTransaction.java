package keyhub.distributedtransactionkit.core.transaction;

import keyhub.distributedtransactionkit.core.compensation.CompensatingTransactionStore;
import keyhub.distributedtransactionkit.core.lib.ApplicationContextProvider;
import keyhub.distributedtransactionkit.core.outbox.OutboxTransactionStore;

import lombok.Getter;

public abstract class AbstractTransaction implements KhTransaction {
    @Getter
    protected final TransactionId transactionId;
    protected final CompensatingTransactionStore compensatingTransactionStore;
    protected final OutboxTransactionStore outboxTransactionStore;

    protected AbstractTransaction(CompensatingTransactionStore compensatingTransactionStore, OutboxTransactionStore outboxTransactionStore) {
        this.transactionId = TransactionId.ofUuid();
        this.compensatingTransactionStore = compensatingTransactionStore;
        this.outboxTransactionStore = outboxTransactionStore;
    }

    protected AbstractTransaction(){
        this.transactionId = TransactionId.ofUuid();
        this.compensatingTransactionStore = ApplicationContextProvider.getApplicationContext().getBean(CompensatingTransactionStore.class);
        this.outboxTransactionStore = ApplicationContextProvider.getApplicationContext().getBean(OutboxTransactionStore.class);
    }

    protected abstract void storeCompensation();

    protected abstract void storeOutbox();
}
