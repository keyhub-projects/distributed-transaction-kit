package keyhub.distributedtransactionkit.starter.component;

import keyhub.distributedtransactionkit.core.exception.KhTransactionRuntimeException;
import keyhub.distributedtransactionkit.core.transaction.KhTransaction;
import keyhub.distributedtransactionkit.starter.event.AfterTransactionEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AfterTransactionEventHandler {
    @EventListener
    public void handleOutboxResolveEvent(AfterTransactionEvent event) {
        KhTransaction transaction = event.getTransaction();
        try {
            log.info("Received resolve event: {}", transaction);
            transaction.resolve();
        } catch (Exception exception) {
            log.error("Failed to resolve transaction", exception);
            throw new KhTransactionRuntimeException(exception);
        }
    }
}
