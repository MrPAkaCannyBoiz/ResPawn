using System;
using System.Collections.Generic;
using System.Text;

namespace ApiContracts.Dtos;

public class PawnshopAddressDto
{
    public int AddressId { get; set; }
    public string StreetName { get; set; } = string.Empty;
    public string SecondaryUnit { get; set; } = string.Empty;
    public string City { get; set; } = string.Empty;
    public int PostalCode { get; set; }
    public int PawnshopId { get; set; }
}
