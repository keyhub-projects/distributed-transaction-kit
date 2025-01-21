package keyhub.distributedtransactionkit.core.transaction;

import com.fasterxml.jackson.core.JsonProcessingException;
import keyhub.distributedtransactionkit.core.context.KhTransactionContext;
import keyhub.distributedtransactionkit.core.exception.KhTransactionException;

import java.util.List;
import java.util.function.Supplier;

public interface KhTransaction {
    TransactionId getTransactionId();

    KhTransaction setCompensation(Supplier<KhTransaction> compensationSupplier);

    KhTransaction setCompensation(KhTransaction compensation);

    KhTransaction setOutbox(Supplier<KhTransaction> outboxSupplier);

    KhTransaction setOutbox(KhTransaction outbox);

    KhTransactionContext getContext();

    interface Result {
        <R> R toData(Class<R> returnType) throws JsonProcessingException;
        <R> List<R> toList(Class<R> returnType);
    }

    Result resolve() throws KhTransactionException;
}
