package keyhub.distributedtransactionkit.sandbox.component;

import keyhub.distributedtransactionkit.core.exception.KhTransactionException;
import keyhub.distributedtransactionkit.core.transaction.remote.RemoteTransaction;
import keyhub.distributedtransactionkit.sandbox.dto.OrderOut;
import keyhub.distributedtransactionkit.sandbox.dto.PaymentOut;
import keyhub.distributedtransactionkit.sandbox.entity.Order;
import keyhub.distributedtransactionkit.starter.adptor.RemoteFrameworkTransaction;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class PaymentClient {

    @Value("client.payment.url")
    private String paymentsUrl;

    public Optional<PaymentOut> findPaymentByOrder(OrderOut orderOut) {
        RemoteTransaction transaction = RemoteFrameworkTransaction.of()
                .get(paymentsUrl+"/"+orderOut.id());
        try{
            return transaction.resolve()
                    .optional(PaymentOut.class);
        } catch (KhTransactionException e){
            return Optional.empty();
        }
    }

    public PaymentOut pay(Order order) {
        RemoteTransaction transaction = RemoteFrameworkTransaction.of()
                .post(paymentsUrl+"/", order)
                .setCompensation(RemoteFrameworkTransaction.of()
                        .post(paymentsUrl, order)
                );
        try{
            return transaction.resolve()
                    .get(PaymentOut.class);
        } catch (KhTransactionException e){
            throw  new RuntimeException("Cannot pay", e);
        }
    }
}
