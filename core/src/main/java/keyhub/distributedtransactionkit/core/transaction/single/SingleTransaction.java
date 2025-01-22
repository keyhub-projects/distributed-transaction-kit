package keyhub.distributedtransactionkit.core.transaction.single;

import keyhub.distributedtransactionkit.core.context.KhTransactionContext;
import keyhub.distributedtransactionkit.core.transaction.KhTransaction;

import java.util.function.Supplier;

public interface SingleTransaction<T> extends KhTransaction {

    static <T> SimpleSingleTransaction<T> of(Supplier<T> transactionProcess, KhTransactionContext transactionContext) {
        return SimpleSingleTransaction.of(transactionProcess, transactionContext);
    }
}
