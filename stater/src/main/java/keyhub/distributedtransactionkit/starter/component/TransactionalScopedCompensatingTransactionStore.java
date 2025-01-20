package keyhub.distributedtransactionkit.starter.component;

import keyhub.distributedtransactionkit.core.compensation.AbstractCompensatingTransactionStore;
import keyhub.distributedtransactionkit.core.transaction.KhTransaction;

public class TransactionalScopedCompensatingTransactionStore extends AbstractCompensatingTransactionStore {
    @Override
    public void add(KhTransaction compensationTransaction) {

    }

    @Override
    public void compensate() {

    }

    @Override
    public void clear() {

    }
    // @Transactional 전후 aop로 처리
}
