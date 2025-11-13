package org.example.respawnmarket.entities;

import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public class CartProductId // composite primary key class for CartProductEntity
{
    private int cartId;

    private int productId;

    public CartProductId()
    {
    }

    public CartProductId(int cartId, int productId)
    {
        this.cartId = cartId;
        this.productId = productId;
    }

    public int getCartId()
    {
        return cartId;
    }

    public void setCartId(int cartId)
    {
        this.cartId = cartId;
    }

    public int getProductId()
    {
        return productId;
    }

    public void setProductId(int productId)
    {
        this.productId = productId;
    }

    @Override
    public boolean equals(Object o)
    {
        if (o == null || getClass() != o.getClass()) return false;
        CartProductId that = (CartProductId) o;
        return cartId == that.cartId && productId == that.productId;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(cartId, productId);
    }
}
