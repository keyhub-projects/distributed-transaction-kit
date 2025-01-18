package keyhub.distributedtransactionkit.core.compensation;

import keyhub.distributedtransactionkit.core.transaction.RemoteTransaction;

public interface CompensatingTransactionStore {
    void add(RemoteTransaction compensationTransaction);
    void compensate();
    void clear();
}
