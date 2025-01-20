package keyhub.distributedtransactionkit.core.transaction;

import keyhub.distributedtransactionkit.core.context.KhTransactionContext;

import keyhub.distributedtransactionkit.core.lib.ApplicationContextProvider;
import lombok.Getter;

public abstract class AbstractTransaction implements KhTransaction {
    @Getter
    protected final TransactionId transactionId;
    protected final KhTransactionContext transactionContext;

    protected AbstractTransaction(KhTransactionContext transactionContext) {
        this.transactionId = TransactionId.ofUuid();
        this.transactionContext = transactionContext;
    }

    protected AbstractTransaction(){
        this.transactionId = TransactionId.ofUuid();
        this.transactionContext = ApplicationContextProvider.getApplicationContext().getBean(KhTransactionContext.class);
    }

    protected abstract void storeCompensation();
    protected abstract void storeOutbox();
}
