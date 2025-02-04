package keyhub.distributedtransactionkit.sandbox.service;

import keyhub.distributedtransactionkit.core.transaction.remote.RemoteTransaction;
import keyhub.distributedtransactionkit.sandbox.component.OrderConverter;
import keyhub.distributedtransactionkit.sandbox.component.PaymentClient;
import keyhub.distributedtransactionkit.sandbox.dto.*;
import keyhub.distributedtransactionkit.sandbox.entity.Order;
import keyhub.distributedtransactionkit.sandbox.persistence.OrderRepository;
import keyhub.distributedtransactionkit.starter.adptor.RemoteFrameworkTransaction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class OrderService {
    private final OrderRepository repository;
    private final OrderConverter converter;
    private final PaymentClient paymentClient;

    @Transactional(readOnly = true)
    public List<OrderOut> list() {
        var result = repository.findAll();
        return result.stream().map(converter::toDto).toList();
    }

    @Transactional(readOnly = true)
    public List<OrderDetailOut> listDetails() {
        List<Order> orders = repository.findAll();
        return orders.stream()
                .map(order -> {
                    OrderOut orderOut = converter.toDto(order);
                    PaymentOut paymentOut = paymentClient.findPaymentByOrder(orderOut)
                            .orElse(null);
                    return new OrderDetailOut(orderOut, paymentOut);
                })
                .toList();
    }

    @Transactional
    public OrderDetailOut order(OrderIn inputDto) {
        Order order = converter.toEntity(inputDto);
        repository.save(order);
        RemoteTransaction transaction = RemoteFrameworkTransaction.of();
        PaymentOut paymentOut = paymentClient.pay(order);
        return new OrderDetailOut(converter.toDto(order), paymentOut);
    }
}
