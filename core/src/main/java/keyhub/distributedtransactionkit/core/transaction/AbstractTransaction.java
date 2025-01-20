package keyhub.distributedtransactionkit.core.transaction;

import keyhub.distributedtransactionkit.core.context.KhTransactionContext;

import keyhub.distributedtransactionkit.core.lib.ApplicationContextProvider;
import lombok.Getter;

import java.util.function.Supplier;

public abstract class AbstractTransaction implements KhTransaction {
    @Getter
    protected final TransactionId transactionId;
    protected final KhTransactionContext transactionContext;
    protected KhTransaction compensation;
    protected KhTransaction outbox;

    protected AbstractTransaction(KhTransactionContext transactionContext) {
        this.transactionId = TransactionId.ofUuid();
        this.transactionContext = transactionContext;
    }

    protected AbstractTransaction(){
        this.transactionId = TransactionId.ofUuid();
        this.transactionContext = ApplicationContextProvider.getApplicationContext().getBean(KhTransactionContext.class);
    }

    @Override
    public KhTransaction setCompensation(Supplier<KhTransaction> compensationSupplier) {
        KhTransaction transaction = compensationSupplier.get();
        return setCompensation(transaction);
    }

    @Override
    public KhTransaction setCompensation(KhTransaction compensation) {
        this.compensation = compensation;
        return this;
    }

    @Override
    public KhTransaction setOutbox(Supplier<KhTransaction> outboxSupplier) {
        KhTransaction transaction = outboxSupplier.get();
        return setOutbox(transaction);
    }

    @Override
    public KhTransaction setOutbox(KhTransaction outbox) {
        this.outbox = outbox;
        return this;
    }

    protected abstract void storeCompensation();
    protected abstract void storeOutbox();
}
