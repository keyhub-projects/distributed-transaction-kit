package keyhub.distributedtransactionkit.sandbox.dto;

public record OrderDetailOut(
        OrderOut order,
        PaymentOut payment
) implements OutputDto{

}
