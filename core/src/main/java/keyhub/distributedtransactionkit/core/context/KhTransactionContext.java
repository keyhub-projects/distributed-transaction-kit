package keyhub.distributedtransactionkit.core.context;

import keyhub.distributedtransactionkit.core.exception.KhCompensationException;
import keyhub.distributedtransactionkit.core.exception.KhOutboxException;
import keyhub.distributedtransactionkit.core.transaction.KhTransaction;
import keyhub.distributedtransactionkit.core.transaction.TransactionId;

public interface KhTransactionContext {

    void compensate(TransactionId transactionId) throws KhCompensationException;

    void invokeEvent(TransactionId transactionId) throws KhOutboxException;

    void storeCompensation(KhTransaction compensation);

    void storeOutbox(KhTransaction outbox);
}
