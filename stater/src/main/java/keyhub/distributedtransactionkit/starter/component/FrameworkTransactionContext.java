package keyhub.distributedtransactionkit.starter.component;

import keyhub.distributedtransactionkit.core.context.AbstractTransactionContext;
import keyhub.distributedtransactionkit.core.exception.KhCompensationException;
import keyhub.distributedtransactionkit.core.exception.KhOutboxException;
import keyhub.distributedtransactionkit.core.exception.KhTransactionRuntimeException;
import keyhub.distributedtransactionkit.core.transaction.KhTransaction;
import keyhub.distributedtransactionkit.core.transaction.TransactionId;
import keyhub.distributedtransactionkit.starter.event.AfterTransactionEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;

import static org.springframework.web.context.WebApplicationContext.SCOPE_REQUEST;

@Component
@Scope("thread") // thread 가 맞을까 request 가 맞을까..
public class FrameworkTransactionContext extends AbstractTransactionContext implements TransactionSynchronization {

    private final ApplicationEventPublisher eventPublisher;

    public FrameworkTransactionContext(ApplicationEventPublisher applicationEventPublisher) {
        super();
        this.eventPublisher = applicationEventPublisher;
    }

    public void invokeAfterTransactionEvent(KhTransaction transaction) {
        AfterTransactionEvent event = new AfterTransactionEvent(transaction);
        eventPublisher.publishEvent(event);
    }

    @Override
    public void afterCompletion(int status) {
        try{
            if(TransactionSynchronizationManager.isActualTransactionActive()) {
                TransactionSynchronizationManager.clear();
            }
            switch (status) {
                case STATUS_COMMITTED -> invokeEvent();
                case STATUS_ROLLED_BACK -> compensate();
            }
        } catch (Exception exception) {
            throw new KhTransactionRuntimeException(exception);
        }
    }

    @Override
    public void compensate(TransactionId transactionId) throws KhCompensationException {
        KhTransaction transaction = compensationStore.pop(transactionId);
        outboxStore.poll(transactionId);
        invokeAfterTransactionEvent(transaction);
    }

    @Override
    public void invokeEvent(TransactionId transactionId) throws KhOutboxException {
        KhTransaction transaction = outboxStore.poll(transactionId);
        compensationStore.pop(transactionId);
        invokeAfterTransactionEvent(transaction);
    }

    @Override
    public void compensate() {
        List<KhTransaction> compensations = compensationStore.popAll();
        compensations.forEach(this::invokeAfterTransactionEvent);
    }

    @Override
    public void invokeEvent() {
        List<KhTransaction> outboxes = outboxStore.pollAll();
        outboxes.forEach(this::invokeAfterTransactionEvent);
    }
}
