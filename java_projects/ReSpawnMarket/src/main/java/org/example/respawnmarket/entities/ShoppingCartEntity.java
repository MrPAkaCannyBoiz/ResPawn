package org.example.respawnmarket.entities;
import jakarta.persistence.*;

@Entity
@Table(name = "shopping_cart")
public class ShoppingCartEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column (name = "total_price", nullable = false)
    private int totalPrice;

    public ShoppingCartEntity()
    {}

    public ShoppingCartEntity(int id, int totalPrice){
        this.id = id;
        this.totalPrice = totalPrice;
    }

    public int getId()
    {
        return id;
    }

    public int getTotalPrice(){
        return totalPrice;
    }

    public void setTotalPrice(int totalPrice){
        this.totalPrice = totalPrice;
    }
}
