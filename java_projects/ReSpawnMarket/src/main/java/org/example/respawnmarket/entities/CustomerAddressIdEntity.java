package org.example.respawnmarket.entities;

import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public class CustomerAddressIdEntity
{
    private int customerId;
    private int addressId;

    public CustomerAddressIdEntity()
    {
    }

    public CustomerAddressIdEntity(int customerId, int addressId)
    {
        this.customerId = customerId;
        this.addressId = addressId;
    }


    public int getCustomerId()
    {
        return customerId;
    }

    public void setCustomerId(int customerId)
    {
        this.customerId = customerId;
    }

    public int getAddressId()
    {
        return addressId;
    }

    public void setAddressId(int addressId)
    {
        this.addressId = addressId;
    }

    @Override
    public boolean equals(Object o)
    {
        if (o == null || getClass() != o.getClass()) return false;
        CustomerAddressIdEntity that = (CustomerAddressIdEntity) o;
        return customerId == that.customerId && addressId == that.addressId;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(customerId, addressId);
    }
}
