package keyhub.distributedtransactionkit.core.transaction.single;

import keyhub.distributedtransactionkit.core.KhTransactionException;
import keyhub.distributedtransactionkit.core.context.compensation.CompensationStore;
import keyhub.distributedtransactionkit.core.context.outbox.OutboxStore;
import keyhub.distributedtransactionkit.core.transaction.KhTransaction;

import java.util.function.Supplier;

public class SimpleSingleTransaction<R extends KhTransaction.Result> extends AbstractSingleTransaction {

    private final Supplier<R> transactionProcess;

    @Override
    public KhTransaction.Result resolve() throws KhTransactionException {
        try {
            var result = transactionProcess.get();
            storeOutbox();
            return result;
        } catch (Exception e) {
            storeCompensation();
            throw new KhTransactionException(e);
        }
    }

    public SimpleSingleTransaction(Supplier<R> transactionProcess) {
        super();
        this.transactionProcess = transactionProcess;
    }

    public SimpleSingleTransaction(Supplier<R> transactionProcess, CompensationStore compensatingTransactionStore, OutboxStore outboxTransactionStore) {
        super(compensatingTransactionStore, outboxTransactionStore);
        this.transactionProcess = transactionProcess;
    }

    public static <R extends KhTransaction.Result> SimpleSingleTransaction<R> of(Supplier<R> transactionProcess) {
        return new SimpleSingleTransaction<>(transactionProcess);
    }

    public static <R extends KhTransaction.Result> SimpleSingleTransaction<R> of(Supplier<R> transactionProcess, CompensationStore compensatingTransactionStore, OutboxStore outboxTransactionStore) {
        return new SimpleSingleTransaction<>(transactionProcess, compensatingTransactionStore, outboxTransactionStore);
    }
}
