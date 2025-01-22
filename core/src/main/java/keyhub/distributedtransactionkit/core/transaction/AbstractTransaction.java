package keyhub.distributedtransactionkit.core.transaction;

import keyhub.distributedtransactionkit.core.context.KhTransactionContext;

import lombok.Getter;

import java.util.function.Supplier;

public abstract class AbstractTransaction implements KhTransaction {
    @Getter
    protected final TransactionId transactionId;
    @Getter
    protected final KhTransactionContext context;
    protected KhTransaction compensation;
    protected KhTransaction outbox;

    protected AbstractTransaction(KhTransactionContext transactionContext) {
        this.transactionId = TransactionId.ofUuid();
        this.context = transactionContext;
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

    protected void storeCompensation() {
        if(compensation != null) {
            this.context.storeCompensation(this.transactionId, this.compensation);
        }
    }

    protected void storeOutbox() {
        if(outbox != null) {
            this.context.storeOutbox(this.transactionId, this.outbox);
        }
    }
}
