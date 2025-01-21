package keyhub.distributedtransactionkit.core.transaction.single;

import keyhub.distributedtransactionkit.core.exception.KhTransactionException;
import keyhub.distributedtransactionkit.core.context.KhTransactionContext;
import keyhub.distributedtransactionkit.core.transaction.AbstractTransaction;

public abstract class AbstractSingleTransaction extends AbstractTransaction implements SingleTransaction {
    protected KhTransactionException exception;
    protected Object rawResult;

    protected AbstractSingleTransaction(KhTransactionContext transactionContext) {
        super(transactionContext);
    }
}
