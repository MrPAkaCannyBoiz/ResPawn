using System;
using System.Collections.Generic;
using System.Text;

namespace ApiContracts.Dtos;

public class CustomerLoginResponseDto
{
    public required int CustomerId { get; set; }
    public string FirstName { get; set; } = string.Empty;
    public string LastName { get; set; } = string.Empty;
    public required string Email { get; set; }
    public string PhoneNumber { get; set; } = string.Empty;
    public bool CanSell { get; set; }

}
