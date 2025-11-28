using System;

namespace ApiContracts.Dtos;

public class CustomerDto
{
    public required string FirstName { get; set; }
    public required string LastName { get; set;}
    public required int Id { get; set; }
    public string Email { get; set; }
    public string PhoneNumber { get; set; }
    public string StreetName { get; set; }
    public int PostalCode { get; set; }
    public string City { get; set; }
    public string SecondaryUnit { get; set; }
    public string Password { get; set; }


}
