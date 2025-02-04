package keyhub.distributedtransactionkit.sandbox;

import keyhub.distributedtransactionkit.starter.annotation.EnableKhTransaction;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableKhTransaction
@SpringBootApplication
public class StarterSandboxApplication {
    public static void main(String[] args) {
        SpringApplication.run(StarterSandboxApplication.class, args);
    }
}
