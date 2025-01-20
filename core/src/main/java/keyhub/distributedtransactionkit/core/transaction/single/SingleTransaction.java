package keyhub.distributedtransactionkit.core.transaction.single;

import keyhub.distributedtransactionkit.core.context.KhTransactionContext;
import keyhub.distributedtransactionkit.core.transaction.KhTransaction;

import java.util.function.Supplier;

public interface SingleTransaction extends KhTransaction {

    static <R extends KhTransaction.Result> SingleTransaction of(Supplier<R> transactionProcess){
        return SimpleSingleTransaction.of(transactionProcess);
    }

    static <R extends KhTransaction.Result> SimpleSingleTransaction<R> of(Supplier<R> transactionProcess, KhTransactionContext transactionContext) {
        return SimpleSingleTransaction.of(transactionProcess, transactionContext);
    }
}
