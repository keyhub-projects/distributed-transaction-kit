package keyhub.distributedtransactionkit.core.exception;

public class KhTransactionRuntimeException extends RuntimeException {

    public KhTransactionRuntimeException() {
    }
    public KhTransactionRuntimeException(String message) {
        super(message);
    }
    public KhTransactionRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
    public KhTransactionRuntimeException(Throwable cause) {
        super(cause);
    }
}
