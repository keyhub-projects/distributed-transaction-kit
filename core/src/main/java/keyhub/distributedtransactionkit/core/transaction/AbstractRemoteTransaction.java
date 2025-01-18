package keyhub.distributedtransactionkit.core.transaction;

import com.fasterxml.jackson.databind.ObjectMapper;
import keyhub.distributedtransactionkit.core.compensation.CompensatingTransactionStore;
import keyhub.distributedtransactionkit.core.lib.UuidV7Generator;
import lombok.Getter;

public abstract class AbstractRemoteTransaction implements RemoteTransaction {
    @Getter
    protected final String transactionId;
    protected final CompensatingTransactionStore compensatingTransactionStore;
    protected final ObjectMapper objectMapper;

    protected AbstractRemoteTransaction(CompensatingTransactionStore compensatingTransactionStore, ObjectMapper objectMapper) {
        this.transactionId = UuidV7Generator.generate().toString();
        this.compensatingTransactionStore = compensatingTransactionStore;
        this.objectMapper = objectMapper;
    }
}
