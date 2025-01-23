package keyhub.distributedtransactionkit.core.transaction.composite;

import keyhub.distributedtransactionkit.core.exception.KhTransactionException;
import keyhub.distributedtransactionkit.core.context.KhTransactionContext;
import keyhub.distributedtransactionkit.core.transaction.KhTransaction;
import keyhub.distributedtransactionkit.core.transaction.TransactionId;

import java.util.Map;

public class SimpleCompositeTransaction extends AbstractCompositeTransaction implements CompositeTransaction {

    protected SimpleCompositeTransaction(KhTransactionContext transactionContext) {
        super(transactionContext);
    }

    @Override
    public SimpleCompositeTransaction.Result resolve() throws KhTransactionException {
        for(TransactionId transactionId : subTransactionMap.keySet()) {
            KhTransaction transaction = subTransactionMap.get(transactionId);
            var result = transaction.resolve();
            subTransactionResultMap.put(transactionId, result);
        }
        try{
            return new SimpleCompositeTransaction.Result(this);
        }finally {
            subTransactionResultMap.clear();
        }
    }

    @Override
    public SimpleCompositeTransaction add(KhTransaction transaction) {
        subTransactionMap.put(transaction.getTransactionId(), transaction);
        return this;
    }

    public record Result(
            Map<TransactionId, KhTransaction.Result<?>> results
    ) implements CompositeTransaction.Result {
        public Result(SimpleCompositeTransaction transaction) {
            this(Map.copyOf(transaction.subTransactionResultMap));
        }

        @Override
        public KhTransaction.Result<?> get(TransactionId transactionId) {
            return results.get(transactionId);
        }

        @Override
        public Map<TransactionId, KhTransaction.Result<?>> get() {
            return results;
        }

        @Override
        public <R> R get(Class<R> returnType) {
            return returnType.cast(results);
        }
    }
}
