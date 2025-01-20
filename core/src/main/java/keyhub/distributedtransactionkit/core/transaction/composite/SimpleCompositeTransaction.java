package keyhub.distributedtransactionkit.core.transaction.composite;

import keyhub.distributedtransactionkit.core.KhTransactionException;
import keyhub.distributedtransactionkit.core.context.KhTransactionContext;
import keyhub.distributedtransactionkit.core.transaction.AbstractTransaction;
import keyhub.distributedtransactionkit.core.transaction.KhTransaction;
import keyhub.distributedtransactionkit.core.transaction.TransactionId;

import java.util.HashMap;
import java.util.Map;

public class SimpleCompositeTransaction extends AbstractTransaction implements CompositeTransaction {

    protected final Map<TransactionId, KhTransaction> transactionIdMap;
    protected KhTransaction compensation;
    protected KhTransaction outbox;
    protected KhTransactionException exception;

    protected SimpleCompositeTransaction(KhTransactionContext transactionContext) {
        super(transactionContext);
        this.transactionIdMap = new HashMap<>();
    }

    @Override
    protected void storeCompensation() {
        //todo
    }

    @Override
    protected void storeOutbox() {
        //todo
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
