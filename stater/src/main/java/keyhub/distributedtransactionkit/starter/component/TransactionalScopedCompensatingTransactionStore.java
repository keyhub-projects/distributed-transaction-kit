package keyhub.distributedtransactionkit.starter.component;

import keyhub.distributedtransactionkit.core.compensation.AbstractCompensatingTransactionStore;
import keyhub.distributedtransactionkit.core.transaction.RemoteTransaction;

public class TransactionalScopedCompensatingTransactionStore extends AbstractCompensatingTransactionStore {
    @Override
    public void add(RemoteTransaction compensationTransaction) {

    }

    @Override
    public void compensate() {

    }

    @Override
    public void clear() {

    }
    // @Transactional 전후 aop로 처리
}
