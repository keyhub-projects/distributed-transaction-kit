package keyhub.distributedtransactionkit.core.transaction.composite;

import keyhub.distributedtransactionkit.core.context.KhTransactionContext;
import keyhub.distributedtransactionkit.core.transaction.AbstractTransaction;
import keyhub.distributedtransactionkit.core.transaction.KhTransaction;
import keyhub.distributedtransactionkit.core.transaction.TransactionId;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractCompositeTransaction extends AbstractTransaction implements CompositeTransaction {

    protected final Map<TransactionId, KhTransaction> subTransactionMap;
    protected final Map<TransactionId, KhTransaction.Result<?>> subTransactionResultMap;

    protected AbstractCompositeTransaction(KhTransactionContext transactionContext) {
        super(transactionContext);
        this.subTransactionMap = new ConcurrentHashMap<>();
        this.subTransactionResultMap = new HashMap<>();
    }
}
