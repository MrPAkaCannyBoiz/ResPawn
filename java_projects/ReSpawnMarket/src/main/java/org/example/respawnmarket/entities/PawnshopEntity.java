package org.example.respawnmarket.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "pawnshop")
public class PawnshopEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "name", nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "address_id", nullable = false)
    private AddressEntity address; // FK to AddressEntity

    public PawnshopEntity()
    {
    }

    public PawnshopEntity(String name, AddressEntity address)
    {
        this.name = name;
        this.address = address;
    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public AddressEntity getAddress()
    {
        return address;
    }

    public void setAddress(AddressEntity address)
    {
        this.address = address;
    }
}
