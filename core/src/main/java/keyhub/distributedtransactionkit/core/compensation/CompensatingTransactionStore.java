package keyhub.distributedtransactionkit.core.compensation;

import keyhub.distributedtransactionkit.core.transaction.KhTransaction;

public interface CompensatingTransactionStore {
    void add(KhTransaction compensationTransaction);
    void compensate();
    void clear();
}
