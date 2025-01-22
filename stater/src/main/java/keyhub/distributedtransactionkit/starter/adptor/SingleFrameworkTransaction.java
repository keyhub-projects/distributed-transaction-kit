package keyhub.distributedtransactionkit.starter.adptor;

import keyhub.distributedtransactionkit.core.transaction.KhTransaction;
import keyhub.distributedtransactionkit.core.transaction.single.SingleTransaction;
import keyhub.distributedtransactionkit.starter.component.FrameworkTransactionContext;

import java.util.function.Supplier;

public class SingleFrameworkTransaction<T> extends FrameworkTransaction implements SingleTransaction<T> {
    protected SingleFrameworkTransaction(SingleTransaction<T> innerTransaction) {
        super(innerTransaction);
    }

    public static <T> SingleFrameworkTransaction<T> of(Supplier<T> transactionProcess) {
        FrameworkTransactionContext context = FrameworkTransaction.getTransactionContext();
        SingleTransaction<T> transaction = SingleTransaction.of(transactionProcess, context);
        return new SingleFrameworkTransaction<>(transaction);
    }
}
