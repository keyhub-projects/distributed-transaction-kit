package keyhub.distributedtransactionkit.starter.component;

import keyhub.distributedtransactionkit.starter.adptor.ApplicationContextProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KhTransactionKitApplicationConfig {
    @Bean
    public ApplicationContextProvider applicationContextProvider() {
        return new ApplicationContextProvider();
    }
}
