package org.example.respawnmarket.dtos;

public class PawnshopAddressPostalDto
{
    private int addressId;
    private String streetName;
    private String secondaryUnit;
    private int postalCode;
    private String city;
    private int pawnshopId;

    public PawnshopAddressPostalDto(int addressId, String streetName, String secondaryUnit, int postalCode, String city,
                                    int pawnshopId)
    {
        this.addressId = addressId;
        this.streetName = streetName;
        this.secondaryUnit = secondaryUnit;
        this.postalCode = postalCode;
        this.city = city;
        this.pawnshopId = pawnshopId;
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

    public int getAddressId()
    {
        return addressId;
    }

    public void setAddressId(int addressId)
    {
        this.addressId = addressId;
    }

    public int getPawnshopId()
    {
        return pawnshopId;
    }

    public void setPawnshopId(int pawnshopId)
    {
        this.pawnshopId = pawnshopId;
    }
}
