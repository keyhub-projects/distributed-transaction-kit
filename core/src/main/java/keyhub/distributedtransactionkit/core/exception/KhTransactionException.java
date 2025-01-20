package keyhub.distributedtransactionkit.core.exception;

import keyhub.distributedtransactionkit.core.transaction.TransactionId;

public class KhTransactionException extends Exception {
    TransactionId transactionId;

    public KhTransactionException(TransactionId transactionId) {
        this.transactionId = transactionId;
    }
    public KhTransactionException(TransactionId transactionId, String message) {
        super(message);
        this.transactionId = transactionId;
    }
    public KhTransactionException(TransactionId transactionId, String message, Throwable cause) {
        super(message, cause);
        this.transactionId = transactionId;
    }
    public KhTransactionException(TransactionId transactionId, Throwable cause) {
        super(cause);
        this.transactionId = transactionId;
    }
}
