package keyhub.distributedtransactionkit.core.transaction.composite;

import keyhub.distributedtransactionkit.core.context.KhTransactionContext;
import keyhub.distributedtransactionkit.core.exception.KhTransactionException;
import keyhub.distributedtransactionkit.core.transaction.KhTransaction;
import keyhub.distributedtransactionkit.core.transaction.TransactionId;

import java.util.*;

public class SimpleSequencedTransaction extends AbstractCompositeTransaction implements SequencedTransaction {
    protected final List<TransactionId> subTransactionIds;

    protected SimpleSequencedTransaction(KhTransactionContext transactionContext) {
        super(transactionContext);
        this.subTransactionIds = new ArrayList<>();
    }

    @Override
    public SimpleSequencedTransaction add(KhTransaction transaction) {
        subTransactionIds.add(transaction.getTransactionId());
        subTransactionMap.put(transaction.getTransactionId(), transaction);
        return this;
    }

    @Override
    public SimpleSequencedTransaction.Result resolve() throws KhTransactionException {
        for(TransactionId transactionId : subTransactionIds) {
            KhTransaction transaction = subTransactionMap.get(transactionId);
            if (transaction == null) {
                continue;
            }
            var result = transaction.resolve();
            subTransactionResultMap.put(transactionId, result);
        }
        try{
            return new SimpleSequencedTransaction.Result(this);
        }finally {
            subTransactionIds.clear();
            subTransactionResultMap.clear();
        }
    }

    public record Result(
            SequencedMap<TransactionId, KhTransaction.Result<?>> results
    ) implements CompositeTransaction.Result {

        Result(SimpleSequencedTransaction transaction) {
            this(new LinkedHashMap<>());
            for(TransactionId id: transaction.subTransactionIds){
                results.put(id, transaction.subTransactionResultMap.get(id));
            }
        }

        @Override
        public KhTransaction.Result<?> get(TransactionId transactionId) {
            return results.get(transactionId);
        }

        @Override
        public SequencedMap<TransactionId, KhTransaction.Result<?>> get() {
            return results;
        }

        @Override
        public <R> R get(Class<R> returnType) {
            return returnType.cast(results);
        }
    }
}
