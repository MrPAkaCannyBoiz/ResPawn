using System;
using System.Collections.Generic;
using System.Text;

namespace ApiContracts.Dtos;

public class UpdateCustomerDto
{
    public string FirstName { get; set; }
    public string LastName { get; set; } 
    public string Email { get; set; }
    public string PhoneNumber { get; set; }
    public string StreetName { get; set; }
    public int PostalCode { get; set; }
    public string City { get; set; }
    public string SecondaryUnit { get; set; } 
}
