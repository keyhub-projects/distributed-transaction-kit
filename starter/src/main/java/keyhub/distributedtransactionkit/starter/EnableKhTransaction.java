package keyhub.distributedtransactionkit.starter;

import keyhub.distributedtransactionkit.starter.component.AfterTransactionEventHandler;
import keyhub.distributedtransactionkit.starter.component.FrameworkTransactionContext;
import keyhub.distributedtransactionkit.starter.component.KhTransactionKitApplicationConfig;
import keyhub.distributedtransactionkit.starter.component.ThreadScopeConfig;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import({
        AfterTransactionEventHandler.class,
        FrameworkTransactionContext.class,
        KhTransactionKitApplicationConfig.class,
        ThreadScopeConfig.class
})
public @interface EnableKhTransaction {
}
