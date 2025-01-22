package keyhub.distributedtransactionkit.starter;

import keyhub.distributedtransactionkit.starter.component.EnableKhTransaction;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableKhTransaction
@SpringBootApplication
public class StarterApplication {
    public static void main(String[] args) {
        SpringApplication.run(StarterApplication.class, args);
    }
}
