using System;
using System.Collections.Generic;
using System.Text;

namespace ApiContracts.Dtos;

public class CustomerLoginDto
{
    public required string Email { get; set; }
    public required string Password { get; set; }
}
