package keyhub.distributedtransactionkit.sandbox.component;

import keyhub.distributedtransactionkit.sandbox.dto.OrderIn;
import keyhub.distributedtransactionkit.sandbox.dto.OrderOut;
import keyhub.distributedtransactionkit.sandbox.entity.Order;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrderConverter {
    OrderOut toDto(Order order);
    Order toEntity(OrderIn orderIn);
}
