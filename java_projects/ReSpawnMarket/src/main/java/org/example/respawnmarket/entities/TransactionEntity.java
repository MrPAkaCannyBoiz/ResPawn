package org.example.respawnmarket.entities;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "transaction")
public class  TransactionEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

  @Column (name = "date", nullable = false)
  private LocalDateTime date;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "shopping_cart_id", nullable = false)
  private  ShoppingCartEntity shoppingCart;

  @ManyToOne
  @JoinColumn(name = "customer_id")
  private CustomerEntity customerId;

  public CustomerEntity getCustomerId()
  {
    return customerId;
  }

  public void setCustomerId(CustomerEntity customerId)
  {
    this.customerId = customerId;
  }

  public TransactionEntity()
  {}

    public TransactionEntity(LocalDateTime date, ShoppingCartEntity shoppingCart)
    {
        this.date = date;
        this.shoppingCart = shoppingCart;
    }

    public int getId()
    {
        return id;
    }

    public LocalDateTime getDate()
    {
        return date;
    }



}
