package keyhub.distributedtransactionkit.core.transaction.single;

import keyhub.distributedtransactionkit.core.exception.KhTransactionException;
import keyhub.distributedtransactionkit.core.context.KhTransactionContext;
import keyhub.distributedtransactionkit.core.transaction.KhTransaction;

import java.util.function.Supplier;

public class SimpleSingleTransaction<R extends KhTransaction.Result> extends AbstractSingleTransaction {

    private final Supplier<R> transactionProcess;

    @Override
    public KhTransaction.Result resolve() throws KhTransactionException {
        try {
            storeCompensation();
            var result = transactionProcess.get();
            storeOutbox();
            return result;
        } catch (Exception e) {
            throw new KhTransactionException(transactionId, e);
        }
    }

    public SimpleSingleTransaction(Supplier<R> transactionProcess) {
        super();
        this.transactionProcess = transactionProcess;
    }

    public SimpleSingleTransaction(Supplier<R> transactionProcess, KhTransactionContext transactionContext) {
        super(transactionContext);
        this.transactionProcess = transactionProcess;
    }

    public static <R extends KhTransaction.Result> SimpleSingleTransaction<R> of(Supplier<R> transactionProcess) {
        return new SimpleSingleTransaction<>(transactionProcess);
    }

    public static <R extends KhTransaction.Result> SimpleSingleTransaction<R> of(Supplier<R> transactionProcess, KhTransactionContext transactionContext) {
        return new SimpleSingleTransaction<>(transactionProcess, transactionContext);
    }
}
