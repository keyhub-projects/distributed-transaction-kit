package keyhub.distributedtransactionkit.core.transaction.composite;

import keyhub.distributedtransactionkit.core.exception.KhTransactionException;
import keyhub.distributedtransactionkit.core.context.KhTransactionContext;
import keyhub.distributedtransactionkit.core.transaction.AbstractTransaction;
import keyhub.distributedtransactionkit.core.transaction.KhTransaction;
import keyhub.distributedtransactionkit.core.transaction.TransactionId;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SimpleCompositeTransaction extends AbstractTransaction implements CompositeTransaction {

    protected final Map<TransactionId, KhTransaction> transactionIdMap;
    protected final Map<TransactionId, KhTransactionException> exceptions;
    protected final Map<TransactionId, Object> rawResults;
    protected KhTransaction compensation;
    protected KhTransaction outbox;

    protected SimpleCompositeTransaction(KhTransactionContext transactionContext) {
        super(transactionContext);
        this.transactionIdMap = new ConcurrentHashMap<>();
        this.exceptions = new HashMap<>();
        this.rawResults = new HashMap<>();
    }

    @Override
    public Result resolve() throws KhTransactionException {
        // todo
        return null;
    }
}
