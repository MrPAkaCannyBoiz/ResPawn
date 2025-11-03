package org.example.respawnmarket.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "customer_address")
public class CustomerAddressEntity
{
    @EmbeddedId
    private CustomerAddressIdEntity id;

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
        this.id = new CustomerAddressIdEntity(customer.getId(), address.getId());
    }

    public CustomerAddressIdEntity getId()
    {
        return id;
    }

    public void setId(CustomerAddressIdEntity id)
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


