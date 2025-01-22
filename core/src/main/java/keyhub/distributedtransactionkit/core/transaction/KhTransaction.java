package keyhub.distributedtransactionkit.core.transaction;

import keyhub.distributedtransactionkit.core.context.KhTransactionContext;
import keyhub.distributedtransactionkit.core.exception.KhTransactionException;

import java.util.function.Supplier;

public interface KhTransaction {
    TransactionId getTransactionId();

    KhTransaction setCompensation(Supplier<KhTransaction> compensationSupplier);

    KhTransaction setCompensation(KhTransaction compensation);

    KhTransaction setOutbox(Supplier<KhTransaction> outboxSupplier);

    KhTransaction setOutbox(KhTransaction outbox);

    KhTransactionContext getContext();

    interface Result<T> {
        T get();
        <R> R get(Class<R> returnType);
    }

    Result<?> resolve() throws KhTransactionException;
}
