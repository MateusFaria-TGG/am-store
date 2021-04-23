package br.com.les.amstore.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.util.List;


@Entity
@Getter
@Setter
@Where(clause = "deleted_at is null")
@ToString
public class Customer extends Person {
    @ManyToOne
    @JoinColumn(name="customer_type_id")
    private CustomerType customerType;

    @OneToMany(mappedBy = "customer", targetEntity = Address.class)
    private List<Address> address;

    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private Wallet wallet;

    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private Cart cart;

    public Customer() {
        this.setRoles("ROLE_CUSTOMER");
        this.setActive(true);
        this.wallet = new Wallet();
        this.cart = new Cart();
    }
}
