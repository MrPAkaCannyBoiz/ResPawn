using System;

namespace ApiContracts.Dtos;

public class CreateCustomerDto
{
    /*
    //proto message of this is:
    message RegisterCustomerRequest
        {
              string first_name = 1;
              string last_name = 2;
              string email = 3;
              string password = 4;
              string phone_number = 5;
              string street_name = 6;
              string secondary_unit = 7;
              int32 postal_code = 8;
              string city = 9;
        }
    therefore, the properties must match these names for easier binding on Blazor client side
    */
    public required string FirstName { get; set; }
    public required string LastName { get; set; }
    public required string Email { get; set; }
    public required string Password { get; set; }
    public required string PhoneNumber { get; set; }
    public required string StreetName { get; set; }
    public string? SecondaryUnit { get; set; }
    public required int PostalCode { get; set; }
    public required string City { get; set; }
}
