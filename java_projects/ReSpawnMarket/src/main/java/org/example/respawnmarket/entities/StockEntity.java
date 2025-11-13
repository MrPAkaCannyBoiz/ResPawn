package org.example.respawnmarket.entities;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "stock")
public class StockEntity // bridge entity between ProductEntity and AddressEntity
{
    @EmbeddedId
    private StockId stockId;

    @MapsId ("productId")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductEntity product; // foreign key to ProductEntity

    @MapsId ("addressId")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id", nullable = false)
    private AddressEntity address; // foreign key to AddressEntity

    @Column (name = "quantity", nullable = false)
    private int availableQuantity;

    public StockEntity()
    {
    }

    public StockEntity(ProductEntity product, AddressEntity address, int availableQuantity)
    {
        this.product = product;
        this.address = address;
        this.availableQuantity = availableQuantity;
        this.stockId = new StockId(product.getId(), address.getId());
    }

    public StockId getStockId()
    {
        return stockId;
    }

    public void setStockId(StockId stockId)
    {
        this.stockId = stockId;
    }

    public ProductEntity getProduct()
    {
        return product;
    }

    public void setProduct(ProductEntity product)
    {
        this.product = product;
    }

    public AddressEntity getAddress()
    {
        return address;
    }

    public void setAddress(AddressEntity address)
    {
        this.address = address;
    }

    public int getAvailableQuantity()
    {
        return availableQuantity;
    }

    public void setAvailableQuantity(int availableQuantity)
    {
        this.availableQuantity = availableQuantity;
    }

    @Override
    public boolean equals(Object o)
    {
        if (o == null || getClass() != o.getClass()) return false;
        StockEntity that = (StockEntity) o;
        return availableQuantity == that.availableQuantity
                && Objects.equals(stockId, that.stockId)
                && Objects.equals(product, that.product)
                && Objects.equals(address, that.address);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(stockId, product, address, availableQuantity);
    }
}
