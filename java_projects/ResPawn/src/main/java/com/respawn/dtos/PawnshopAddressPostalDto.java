package com.respawn.dtos;

public record PawnshopAddressPostalDto(int addressId, String streetName, String secondaryUnit, int postalCode,
                                       String city, int pawnshopId)
{

}
