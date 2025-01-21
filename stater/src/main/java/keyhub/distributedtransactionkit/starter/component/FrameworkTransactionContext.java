package keyhub.distributedtransactionkit.starter.component;

import keyhub.distributedtransactionkit.core.context.AbstractTransactionContext;
import keyhub.distributedtransactionkit.core.exception.KhTransactionRuntimeException;
import keyhub.distributedtransactionkit.starter.adptor.ApplicationContextProvider;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import static org.springframework.web.context.WebApplicationContext.SCOPE_REQUEST;

@Component
@Scope("thread") // thread 가 맞을까 request 가 맞을까..
public class FrameworkTransactionContext extends AbstractTransactionContext implements TransactionSynchronization {

    public FrameworkTransactionContext() {
        super();
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
}
