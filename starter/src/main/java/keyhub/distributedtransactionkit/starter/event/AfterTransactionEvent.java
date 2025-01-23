package keyhub.distributedtransactionkit.starter.event;

import keyhub.distributedtransactionkit.core.transaction.KhTransaction;

public record AfterTransactionEvent(
        KhTransaction transaction
) {
}
