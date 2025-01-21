package keyhub.distributedtransactionkit.core.context;

import keyhub.distributedtransactionkit.core.exception.KhCompensationException;
import keyhub.distributedtransactionkit.core.exception.KhOutboxException;
import keyhub.distributedtransactionkit.core.context.compensation.CompensationStore;
import keyhub.distributedtransactionkit.core.context.outbox.OutboxStore;
import keyhub.distributedtransactionkit.core.exception.KhTransactionRuntimeException;
import keyhub.distributedtransactionkit.core.transaction.KhTransaction;
import keyhub.distributedtransactionkit.core.transaction.TransactionId;

import java.util.List;

public class AbstractTransactionContext implements KhTransactionContext {
    private final CompensationStore compensationStore;
    private final OutboxStore outboxStore;

    public AbstractTransactionContext() {
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
    public void compensate() throws KhCompensationException {
        List<KhTransaction> compensations = compensationStore.popAll();
        compensations.forEach(compensation -> {
            try {
                compensation.resolve();
            } catch (Exception exception) {
                // todo logger
                throw new KhTransactionRuntimeException(exception);
            }
        });
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
    public void invokeEvent() throws KhOutboxException{
        List<KhTransaction> outboxes = outboxStore.pollAll();
        outboxes.forEach(outbox -> {
            try {
                outbox.resolve();
            } catch (Exception exception) {
                // todo logger
                throw new KhTransactionRuntimeException(exception);
            }
        });
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
