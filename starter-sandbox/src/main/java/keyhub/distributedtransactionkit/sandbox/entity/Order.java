package keyhub.distributedtransactionkit.sandbox.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Order {
    @Id
    Long id;
}
