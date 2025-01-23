package keyhub.distributedtransactionkit.starter.adptor;

import keyhub.distributedtransactionkit.core.transaction.KhTransaction;
import keyhub.distributedtransactionkit.core.transaction.composite.CompositeTransaction;
import keyhub.distributedtransactionkit.core.transaction.composite.SequencedTransaction;
import keyhub.distributedtransactionkit.starter.component.FrameworkTransactionContext;

public class SequencedFrameworkTransaction extends FrameworkTransaction implements SequencedTransaction {

    public SequencedFrameworkTransaction(KhTransaction transaction) {
        super(transaction);
    }

    public static SequencedFrameworkTransaction of() {
        FrameworkTransactionContext context = FrameworkTransaction.getTransactionContext();
        SequencedTransaction transaction = SequencedTransaction.from(context);
        return new SequencedFrameworkTransaction(transaction);
    }

    public static SequencedFrameworkTransaction of(KhTransaction ...transactions) {
        SequencedFrameworkTransaction result = of();
        for (KhTransaction transaction : transactions) {
            result.add(transaction);
        }
        return result;
    }

    @Override
    public SequencedFrameworkTransaction add(KhTransaction transaction) {
        ((SequencedTransaction)this.innerTransaction).add(transaction);
        return this;
    }
}
