package keyhub.distributedtransactionkit.core.transaction.composite;

import keyhub.distributedtransactionkit.core.transaction.KhTransaction;

public interface CompositeTransaction extends KhTransaction {
    KhTransaction putCompensation(KhTransaction compensation);
    KhTransaction putOutbox(KhTransaction outbox);
}
