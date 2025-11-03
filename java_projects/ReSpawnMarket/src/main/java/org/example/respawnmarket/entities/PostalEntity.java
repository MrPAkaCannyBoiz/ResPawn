package org.example.respawnmarket.entities;
import jakarta.persistence.*;

@Entity
@Table(name = "postal")
public class PostalEntity
{
    @Id
    @Column (name = "postal_code", nullable = false)
    private int postalCode;

    @Column (name = "city", nullable = false)
    private String city;

    public PostalEntity(){

    }

    public PostalEntity(int postalCode, String city) {
        this.postalCode = postalCode;
        this.city = city;
    }

    public int getPostalCode()
    {
        return postalCode;
    }

    public void setPostalCode(int postalCode)
    {
        this.postalCode = postalCode;
    }

    public String getCity()
    {
        return city;
    }

    public void setCity(String city)
    {
        this.city = city;
    }
}
