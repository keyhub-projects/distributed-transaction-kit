package keyhub.distributedtransactionkit.core.transaction.single;

import keyhub.distributedtransactionkit.core.exception.KhTransactionException;
import keyhub.distributedtransactionkit.core.context.KhTransactionContext;
import keyhub.distributedtransactionkit.core.transaction.KhTransaction;

import java.util.function.Supplier;

public class SimpleSingleTransaction<T> extends AbstractSingleTransaction<T> {

    private final Supplier<T> transactionProcess;

    @Override
    public SimpleSingleTransaction.Result<T> resolve() throws KhTransactionException {
        try {
            var result = transactionProcess.get();
            storeCompensation();
            storeOutbox();
            return new Result<>(result);
        } catch (Exception e) {
            throw new KhTransactionException(transactionId, e);
        }
    }

    public SimpleSingleTransaction(Supplier<T> transactionProcess, KhTransactionContext transactionContext) {
        super(transactionContext);
        this.transactionProcess = transactionProcess;
    }

    public static <T> SimpleSingleTransaction<T> of(Supplier<T> transactionProcess, KhTransactionContext transactionContext) {
        return new SimpleSingleTransaction<>(transactionProcess, transactionContext);
    }

    public static class Result<T> implements KhTransaction.Result<T> {
        T data;

        Result(T data) {
            this.data = data;
        }

        @Override
        public T get(){
            return data;
        }

        @Override
        public <R> R get(Class<R> returnType) {
            return returnType.cast(data);
        }
    }
}
