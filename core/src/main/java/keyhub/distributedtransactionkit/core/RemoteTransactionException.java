package keyhub.distributedtransactionkit.core;

public class RemoteTransactionException extends Exception {
    public RemoteTransactionException() {}
    public RemoteTransactionException(String message) {
        super(message);
    }
    public RemoteTransactionException(String message, Throwable cause) {
        super(message, cause);
    }
    public RemoteTransactionException(Throwable cause) {
        super(cause);
    }
}
