package keyhub.distributedtransactionkit.core.transaction.single;

import keyhub.distributedtransactionkit.core.KhTransactionException;
import keyhub.distributedtransactionkit.core.compensation.CompensatingTransactionStore;
import keyhub.distributedtransactionkit.core.outbox.OutboxTransactionStore;
import keyhub.distributedtransactionkit.core.transaction.AbstractTransaction;
import keyhub.distributedtransactionkit.core.transaction.KhTransaction;

import java.util.function.Supplier;

public abstract class AbstractSingleTransaction extends AbstractTransaction implements SingleTransaction {

    protected KhTransaction compensationTransaction;
    protected KhTransaction outboxTransaction;
    protected KhTransactionException exception;
    protected Object rawResult;

    protected AbstractSingleTransaction(CompensatingTransactionStore compensatingTransactionStore, OutboxTransactionStore outboxTransactionStore) {
        super(compensatingTransactionStore, outboxTransactionStore);
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
        this.compensationTransaction = compensation;
        return this;
    }

    @Override
    protected void storeCompensation() {
        this.outboxTransactionStore.add(this.outboxTransaction);
    }

    @Override
    public KhTransaction setOutbox(Supplier<KhTransaction> outboxSupplier) {
        KhTransaction transaction = outboxSupplier.get();
        return setOutbox(transaction);
    }

    @Override
    public KhTransaction setOutbox(KhTransaction outbox) {
        this.outboxTransaction = outbox;
        return this;
    }

    @Override
    protected void storeOutbox() {
        this.compensatingTransactionStore.add(this.compensationTransaction);
    }
}
