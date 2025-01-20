package keyhub.distributedtransactionkit.core.context.compensation;

import keyhub.distributedtransactionkit.core.transaction.KhTransaction;

public interface CompensationStore {
    void add(KhTransaction compensationTransaction);
    void compensate();
    void clear();
}
