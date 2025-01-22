package keyhub.distributedtransactionkit.core.transaction.single;

import keyhub.distributedtransactionkit.core.exception.KhTransactionException;
import keyhub.distributedtransactionkit.core.context.KhTransactionContext;
import keyhub.distributedtransactionkit.core.transaction.AbstractTransaction;

public abstract class AbstractSingleTransaction<T> extends AbstractTransaction implements SingleTransaction<T> {
    protected KhTransactionException exception;
    protected T rawResult;

    protected AbstractSingleTransaction(KhTransactionContext transactionContext) {
        super(transactionContext);
    }
}
