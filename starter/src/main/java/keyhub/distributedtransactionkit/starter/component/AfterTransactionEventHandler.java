package keyhub.distributedtransactionkit.starter.component;

import keyhub.distributedtransactionkit.core.exception.KhTransactionRuntimeException;
import keyhub.distributedtransactionkit.core.transaction.KhTransaction;
import keyhub.distributedtransactionkit.starter.event.AfterTransactionEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j(topic = "keyhub.transaction.context")
@Component
public class AfterTransactionEventHandler {
    @EventListener
    public void handleOutboxResolveEvent(AfterTransactionEvent event) {
        KhTransaction transaction = event.transaction();
        try {
            log.info("Received resolve event: {}", transaction.getTransactionId());
            transaction.resolve();
        } catch (Exception exception) {
            log.warn("Failed to resolve transaction: {}", transaction.getTransactionId(), exception);
            throw new KhTransactionRuntimeException(exception);
        }
    }
}
