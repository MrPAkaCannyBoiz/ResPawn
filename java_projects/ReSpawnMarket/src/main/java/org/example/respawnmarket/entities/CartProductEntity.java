package org.example.respawnmarket.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "cart_product")
public class CartProductEntity
{
    @EmbeddedId
    private CartProductId id; // composite primary key

    @MapsId ("cartId")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn (name = "shopping_cart_id", nullable = false)
    private ShoppingCartEntity cart; // foreign key to ShoppingCartEntity

    @MapsId ("productId")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn (name = "product_id", nullable = false)
    private ProductEntity product; // foreign key to ProductEntity

    @Column (name = "quantity", nullable = false)
    private int quantity;

    public CartProductEntity()
    {
    }

    public CartProductEntity(ShoppingCartEntity cart, ProductEntity product, int quantity)
    {
        this.cart = cart;
        this.product = product;
        this.quantity = quantity;
        this.id = new CartProductId(cart.getId(), product.getId());
    }

    public CartProductId getId()
    {
        return id;
    }

    public void setId(CartProductId id)
    {
        this.id = id;
    }

    public ShoppingCartEntity getCart()
    {
        return cart;
    }

    public void setCart(ShoppingCartEntity cart)
    {
        this.cart = cart;
    }

    public ProductEntity getProduct()
    {
        return product;
    }

    public void setProduct(ProductEntity product)
    {
        this.product = product;
    }
}
