package keyhub.distributedtransactionkit.sandbox.persistence;

import keyhub.distributedtransactionkit.sandbox.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
