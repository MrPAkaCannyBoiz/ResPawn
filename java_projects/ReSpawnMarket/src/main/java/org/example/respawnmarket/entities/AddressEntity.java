package org.example.respawnmarket.entities;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "address")
public class AddressEntity
{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "street_name", nullable = false)
    private String streetName;

    @Column(name = "secondary_unit", nullable = true)
    private String secondaryUnit;

    @ManyToOne
    @JoinColumn(name = "postal_code", nullable = false)
    private PostalEntity postal;

    // only for bidirectional mapping, not used directly
    @OneToMany(mappedBy = "address", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CustomerAddressEntity> customerAddressEntities = new ArrayList<>();

    public AddressEntity()
    {

    }

    public AddressEntity(String streetName, String secondaryUnit, PostalEntity postal)
    {
        this.streetName = streetName;
        this.secondaryUnit = secondaryUnit;
        this.postal = postal;
    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public String getStreetName()
    {
        return streetName;
    }

    public void setStreetName(String streetName)
    {
        this.streetName = streetName;
    }

    public String getSecondaryUnit()
    {
        return secondaryUnit;
    }

    public void setSecondaryUnit(String secondaryUnit)
    {
        this.secondaryUnit = secondaryUnit;
    }

    public List<CustomerAddressEntity> getCustomerAddresses()
    {
        return customerAddressEntities;
    }

    public PostalEntity getPostal()
    {
        return postal;
    }

    public void setPostal(PostalEntity postal)
    {
        this.postal = postal;
    }
}
