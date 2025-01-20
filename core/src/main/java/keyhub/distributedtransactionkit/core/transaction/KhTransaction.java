package keyhub.distributedtransactionkit.core.transaction;

import com.fasterxml.jackson.core.JsonProcessingException;
import keyhub.distributedtransactionkit.core.KhTransactionException;

import java.util.List;

public interface KhTransaction {
    TransactionId getTransactionId();

    interface Result {
        <R> R toData(Class<R> returnType) throws JsonProcessingException;
        <R> List<R> toList(Class<R> returnType);
    }

    Result resolve() throws KhTransactionException;
}
