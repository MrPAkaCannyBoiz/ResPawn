using System;

namespace ApiContracts;

public class CreateCustomerDto
{
    public required string UserName { get; set; }

    public required string Password { get; set; }
}
