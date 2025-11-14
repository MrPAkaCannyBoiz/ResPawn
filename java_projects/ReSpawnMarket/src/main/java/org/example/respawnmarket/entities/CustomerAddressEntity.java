package org.example.respawnmarket.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "customer_address")
public class CustomerAddressEntity
{
    @EmbeddedId
    private CustomerAddressId id;

    @MapsId ("customerId")
    @ManyToOne (optional = false, fetch = FetchType.LAZY)
    @JoinColumn (name = "customer_id", nullable = false)
    private CustomerEntity customer;

    @MapsId ("addressId")
    @ManyToOne (optional = false, fetch = FetchType.LAZY)
    @JoinColumn (name = "address_id", nullable = false)
    private AddressEntity address;


    public CustomerAddressEntity()
    {
    }

    public CustomerAddressEntity(CustomerEntity customer, AddressEntity address)
    {
        this.customer = customer;
        this.address = address;
        this.id = new CustomerAddressId(customer.getId(), address.getId());
    }

    public CustomerAddressId getId()
    {
        return id;
    }

    public void setId(CustomerAddressId id)
    {
        this.id = id;
    }

    public CustomerEntity getCustomer()
    {
        return customer;
    }

    public void setCustomer(CustomerEntity customer)
    {
        this.customer = customer;
    }

    public AddressEntity getAddress()
    {
        return address;
    }

    public void setAddress(AddressEntity address)
    {
        this.address = address;
    }
}


