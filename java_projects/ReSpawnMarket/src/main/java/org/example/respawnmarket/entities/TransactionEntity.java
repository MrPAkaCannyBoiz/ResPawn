package org.example.respawnmarket.entities;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "transaction")
public class TransactionEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

  @Column (name = "date", nullable = false)
  private LocalDate date;

  @Column(name = "shopping_cart_id", nullable = false)
    private int shoppingCartId;

  public TransactionEntity()
  {}

    public TransactionEntity(LocalDate date, int shoppingCartId)
    {
        this.date = date;
        this.shoppingCartId = shoppingCartId;
    }

    public int getId()
    {
        return id;
    }

    public LocalDate getDate()
    {
        return date;
    }



}
