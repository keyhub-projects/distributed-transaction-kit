package keyhub.distributedtransactionkit.core.transaction.single;

import keyhub.distributedtransactionkit.core.KhTransactionException;
import keyhub.distributedtransactionkit.core.context.KhTransactionContext;
import keyhub.distributedtransactionkit.core.transaction.AbstractTransaction;
import keyhub.distributedtransactionkit.core.transaction.KhTransaction;

import java.util.function.Supplier;

public abstract class AbstractSingleTransaction extends AbstractTransaction implements SingleTransaction {

    protected KhTransaction compensation;
    protected KhTransaction outbox;
    protected KhTransactionException exception;
    protected Object rawResult;

    protected AbstractSingleTransaction(KhTransactionContext transactionContext) {
        super(transactionContext);
    }

    protected AbstractSingleTransaction() {
        super();
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

    @Override
    protected void storeCompensation() {
        this.transactionContext.storeCompensation(this.compensation);
    }

    @Override
    protected void storeOutbox() {
        this.transactionContext.storeOutbox(this.outbox);
    }
}
