package keyhub.distributedtransactionkit.core.context;

import keyhub.distributedtransactionkit.core.exception.KhCompensationException;
import keyhub.distributedtransactionkit.core.exception.KhOutboxException;
import keyhub.distributedtransactionkit.core.context.compensation.CompensationStore;
import keyhub.distributedtransactionkit.core.context.outbox.OutboxStore;
import keyhub.distributedtransactionkit.core.transaction.KhTransaction;
import keyhub.distributedtransactionkit.core.transaction.TransactionId;

public class TransactionContextImplement implements KhTransactionContext {
    private final CompensationStore compensationStore;
    private final OutboxStore outboxStore;

    public TransactionContextImplement() {
        this.compensationStore = CompensationStore.of();
        this.outboxStore = OutboxStore.of();
    }

    @Override
    public void compensate(TransactionId transactionId) throws KhCompensationException {
        KhTransaction transaction = compensationStore.pop(transactionId);
        outboxStore.poll(transactionId);
        try{
            transaction.resolve();
        } catch (Exception exception){
            throw new KhCompensationException(transactionId, exception);
        }
    }

    @Override
    public void invokeEvent(TransactionId transactionId) throws KhOutboxException {
        KhTransaction transaction = outboxStore.poll(transactionId);
        compensationStore.pop(transactionId);
        try{
            transaction.resolve();
        } catch (Exception exception) {
            throw new KhOutboxException(transactionId, exception);
        }
    }

    @Override
    public void storeCompensation(KhTransaction compensation) {
        this.compensationStore.add(compensation);
    }

    @Override
    public void storeOutbox(KhTransaction outbox) {
        this.outboxStore.add(outbox);
    }
}
