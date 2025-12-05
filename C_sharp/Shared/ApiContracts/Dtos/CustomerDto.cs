using System;

namespace ApiContracts.Dtos;

public class CustomerDto
{
    public required string FirstName { get; set; }
    public required string LastName { get; set;}
    public required int Id { get; set; }
    public string Email { get; set; } = string.Empty;
    public string PhoneNumber { get; set; } = string.Empty;
    public string StreetName { get; set; } = string.Empty;
    public int PostalCode { get; set; }
    public string City { get; set; } = string.Empty;
    public string SecondaryUnit { get; set; } = string.Empty;
    public bool CanSell { get; set; } 

}
