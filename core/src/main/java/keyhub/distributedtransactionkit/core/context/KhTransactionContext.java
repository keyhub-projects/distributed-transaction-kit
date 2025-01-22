package keyhub.distributedtransactionkit.core.context;

import keyhub.distributedtransactionkit.core.exception.KhCompensationException;
import keyhub.distributedtransactionkit.core.exception.KhOutboxException;
import keyhub.distributedtransactionkit.core.transaction.KhTransaction;
import keyhub.distributedtransactionkit.core.transaction.TransactionId;

public interface KhTransactionContext {

    void compensate(TransactionId transactionId) throws KhCompensationException;

    void compensate() throws KhCompensationException;

    void invokeEvent(TransactionId transactionId) throws KhOutboxException;

    void invokeEvent() throws KhOutboxException;

    void storeCompensation(TransactionId transactionId, KhTransaction compensation);

    void storeOutbox(TransactionId transactionId, KhTransaction outbox);
}
