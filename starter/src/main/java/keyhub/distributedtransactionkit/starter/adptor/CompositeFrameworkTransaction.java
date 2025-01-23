package keyhub.distributedtransactionkit.starter.adptor;

import keyhub.distributedtransactionkit.core.transaction.KhTransaction;
import keyhub.distributedtransactionkit.core.transaction.composite.CompositeTransaction;
import keyhub.distributedtransactionkit.starter.component.FrameworkTransactionContext;

public class CompositeFrameworkTransaction extends FrameworkTransaction implements CompositeTransaction {

    public CompositeFrameworkTransaction(KhTransaction transaction) {
        super(transaction);
    }

    public static CompositeFrameworkTransaction of() {
        FrameworkTransactionContext context = FrameworkTransaction.getTransactionContext();
        CompositeTransaction transaction = CompositeTransaction.from(context);
        return new CompositeFrameworkTransaction(transaction);
    }

    public static CompositeFrameworkTransaction of(KhTransaction ...transactions) {
        CompositeFrameworkTransaction result = of();
        for (KhTransaction transaction : transactions) {
            result.add(transaction);
        }
        return result;
    }

    @Override
    public CompositeFrameworkTransaction add(KhTransaction transaction) {
        ((CompositeTransaction)this.innerTransaction).add(transaction);
        return this;
    }
}
