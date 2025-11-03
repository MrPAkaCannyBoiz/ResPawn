package org.example.respawnmarket.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "product")

public class ProductEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column (name = "name", nullable = false)
    private String name;

    @Column (name = "price", nullable = false)
    private double price;

    @Column (name = "condition", nullable = false)
    private String condition;

    @Column (name = "description", nullable = false)
    private String description;

    @Column (name = "photo_url", nullable = true)
    private String photoUrl;

    //FK
    @ManyToOne
    @JoinColumn(name = "sold_by_customer", nullable = false)
    private CustomerEntity seller;

    @ManyToOne
    @JoinColumn(name = "shopping_cart_id", nullable = true)
    private ShoppingCartEntity shoppingCart;

   public ProductEntity()
   {
   }
    public ProductEntity(String name, double price, String condition, String description,
                         String photoUrl, CustomerEntity seller, ShoppingCartEntity shoppingCart)
    {
        this.name = name;
        this.price = price;
        this.condition = condition;
        this.description = description;
        this.photoUrl = photoUrl;
        this.seller = seller;
        this.shoppingCart = shoppingCart;
    }

    public int getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public double getPrice()
    {
        return price;
    }

    public String getCondition()
    {
        return condition;
    }

    public String getDescription()
    {
        return description;
    }

    public String getPhotoUrl()
    {
        return photoUrl;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public void setPrice(double price)
    {
        this.price = price;
    }

    public void setCondition(String condition)
    {
        this.condition = condition;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public void setPhotoUrl(String photoUrl)
    {
        this.photoUrl = photoUrl;
    }


    public CustomerEntity getSeller()
    {
        return seller;
    }

    public void setSeller(CustomerEntity seller)
    {
        this.seller = seller;
    }

    public ShoppingCartEntity getShoppingCart()
    {
        return shoppingCart;
    }

    public void setShoppingCart(ShoppingCartEntity shoppingCart)
    {
        this.shoppingCart = shoppingCart;
    }
}
