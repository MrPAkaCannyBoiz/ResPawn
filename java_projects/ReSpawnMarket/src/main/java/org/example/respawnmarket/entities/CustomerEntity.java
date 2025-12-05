package org.example.respawnmarket.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "customer")
public class CustomerEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //only if you have auto increment(i.e. serial) in db
    private int id;

    @Column (name = "first_name", nullable = false)
    private String firstName;

    @Column (name = "last_name", nullable = false)
    private String lastName;

    @Column (name = "email", nullable = false, unique = true)
    private String email;

    @Column (name = "password", nullable = false, unique = false)
    private String password;

    @Column (name = "phone_number", nullable = false, unique = true)
    private String phoneNumber;

    @Column (name = "can_sell" , nullable = false)
    private boolean canSell = true;

    public CustomerEntity()
    {

    }

    public CustomerEntity(String firstName, String lastName, String email
            , String password, String phoneNumber)
    {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.phoneNumber = phoneNumber;
    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public String getFirstName()
    {
        return firstName;
    }

    public void setFirstName(String firstName)
    {
        this.firstName = firstName;
    }

    public String getLastName()
    {
        return lastName;
    }

    public void setLastName(String lastName)
    {
        this.lastName = lastName;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public String getPhoneNumber()
    {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber)
    {
        this.phoneNumber = phoneNumber;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public boolean isCanSell()
    {
        return canSell;
    }

    public void setCanSell(boolean canSell)
    {
        this.canSell = canSell;
    }

}
