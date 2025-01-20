package keyhub.distributedtransactionkit.core.transaction.composite;

import keyhub.distributedtransactionkit.core.KhTransactionException;
import keyhub.distributedtransactionkit.core.RemoteTransactionException;
import keyhub.distributedtransactionkit.core.context.compensation.CompensationStore;
import keyhub.distributedtransactionkit.core.context.outbox.OutboxStore;
import keyhub.distributedtransactionkit.core.transaction.AbstractTransaction;
import keyhub.distributedtransactionkit.core.transaction.KhTransaction;
import keyhub.distributedtransactionkit.core.transaction.TransactionId;

import java.util.HashMap;
import java.util.Map;

public class SimpleCompositeTransaction extends AbstractTransaction implements CompositeTransaction {

    protected final Map<TransactionId, KhTransaction> transactionIdMap;
    protected KhTransaction compensationTransaction;
    protected KhTransaction outboxTransaction;
    protected RemoteTransactionException exception;

    protected SimpleCompositeTransaction(CompensationStore compensatingTransactionStore, OutboxStore outboxTransactionStore) {
        super(compensatingTransactionStore, outboxTransactionStore);
        this.transactionIdMap = new HashMap<>();
    }

    @Override
    protected void storeCompensation() {

    }

    @Override
    protected void storeOutbox() {

    }

    @Override
    public Result resolve() throws KhTransactionException {
        return null;
    }

    @Override
    public SimpleCompositeTransaction putCompensation(KhTransaction compensation) {
        return this;
    }

    @Override
    public SimpleCompositeTransaction putOutbox(KhTransaction outbox) {
        return this;
    }
}
