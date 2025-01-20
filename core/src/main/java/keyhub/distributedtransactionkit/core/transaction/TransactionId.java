package keyhub.distributedtransactionkit.core.transaction;

import keyhub.distributedtransactionkit.core.lib.UuidV7Generator;

public record TransactionId(
        String transactionId
) {
    public static TransactionId fromString(String transactionId) {
        return new TransactionId(transactionId);
    }
    public static TransactionId ofUuid(){
        return new TransactionId(UuidV7Generator.generate().toString());
    }
}
