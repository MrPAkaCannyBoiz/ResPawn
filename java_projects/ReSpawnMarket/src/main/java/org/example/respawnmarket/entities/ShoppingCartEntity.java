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
    private double totalPrice;

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

    public double getTotalPrice(){
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice){
        this.totalPrice = totalPrice;
    }
}
