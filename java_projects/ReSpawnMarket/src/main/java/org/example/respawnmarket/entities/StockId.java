package org.example.respawnmarket.entities;

import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public class StockId // composite key class for StockEntity
{
    private int productId;
    private int addressId;

    public StockId()
    {
    }

    public StockId(int productId, int addressId)
    {
        this.productId = productId;
        this.addressId = addressId;
    }

    public int getProductId()
    {
        return productId;
    }

    public void setProductId(int productId)
    {
        this.productId = productId;
    }

    public int getAddressId()
    {
        return addressId;
    }

    public void setAddressId(int warehouseId)
    {
        this.addressId = warehouseId;
    }

    @Override
    public boolean equals(Object o)
    {
        if (o == null || getClass() != o.getClass()) return false;
        StockId stockId = (StockId) o;
        return productId == stockId.productId && addressId == stockId.addressId;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(productId, addressId);
    }

}
