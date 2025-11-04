package org.example.respawnmarket.dtos;

public class CustomerDto
{
    // customer message dto
    private int customerId;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String phoneNumber;

    // address message dto
    private int address_id;
    private String street_name;
    private String secondary_unit;
    private int postal_code;


    public CustomerDto(int customerId, String firstName, String lastName, String password, String email, String phoneNumber, String street_name, int address_id, String secondary_unit, int postal_code)
    {
        this.customerId = customerId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.password = password;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.street_name = street_name;
        this.address_id = address_id;
        this.secondary_unit = secondary_unit;
        this.postal_code = postal_code;
    }

    public int getCustomerId()
    {
        return customerId;
    }

    public void setCustomerId(int customerId)
    {
        this.customerId = customerId;
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

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public int getAddress_id()
    {
        return address_id;
    }

    public void setAddress_id(int address_id)
    {
        this.address_id = address_id;
    }

    public String getPhoneNumber()
    {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber)
    {
        this.phoneNumber = phoneNumber;
    }

    public String getStreet_name()
    {
        return street_name;
    }

    public void setStreet_name(String street_name)
    {
        this.street_name = street_name;
    }

    public int getPostal_code()
    {
        return postal_code;
    }

    public void setPostal_code(int postal_code)
    {
        this.postal_code = postal_code;
    }

    public String getSecondary_unit()
    {
        return secondary_unit;
    }

    public void setSecondary_unit(String secondary_unit)
    {
        this.secondary_unit = secondary_unit;
    }




}
