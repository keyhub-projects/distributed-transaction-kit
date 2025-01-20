package keyhub.distributedtransactionkit.core;

public class KhTransactionException extends Exception {
    public KhTransactionException() {}
    public KhTransactionException(String message) {
        super(message);
    }
    public KhTransactionException(String message, Throwable cause) {
        super(message, cause);
    }
    public KhTransactionException(Throwable cause) {
        super(cause);
    }
}
