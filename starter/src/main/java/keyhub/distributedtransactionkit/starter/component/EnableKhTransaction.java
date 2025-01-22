package keyhub.distributedtransactionkit.starter.component;

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
