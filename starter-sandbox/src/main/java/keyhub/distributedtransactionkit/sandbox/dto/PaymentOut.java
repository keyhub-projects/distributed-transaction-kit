package keyhub.distributedtransactionkit.sandbox.dto;

public record PaymentOut(
        Long id,
        Long productId
) implements OutputDto {
}
