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
        subTransactionResultMap.clear();
        return new SimpleCompositeTransaction.Result(this);
    }

    @Override
    public SimpleCompositeTransaction add(KhTransaction transaction) {
        subTransactionMap.put(transaction.getTransactionId(), transaction);
        return this;
    }

    public static class Result implements CompositeTransaction.Result {
        Map<TransactionId, KhTransaction.Result<?>> results;

        protected Result(SimpleCompositeTransaction transaction) {
            this.results = transaction.subTransactionResultMap;
            transaction.subTransactionResultMap.clear();
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
