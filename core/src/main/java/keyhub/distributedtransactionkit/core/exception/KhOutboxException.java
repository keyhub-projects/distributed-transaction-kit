package keyhub.distributedtransactionkit.core.exception;

import keyhub.distributedtransactionkit.core.transaction.TransactionId;

public class KhOutboxException extends Exception {
    TransactionId transactionId;

    public KhOutboxException(TransactionId transactionId) {
        this.transactionId = transactionId;
    }
    public KhOutboxException(TransactionId transactionId, String message) {
        super(message);
        this.transactionId = transactionId;
    }
    public KhOutboxException(TransactionId transactionId, String message, Throwable cause) {
        super(message, cause);
        this.transactionId = transactionId;
    }
    public KhOutboxException(TransactionId transactionId, Throwable cause) {
        super(cause);
        this.transactionId = transactionId;
    }
}
