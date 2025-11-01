using System;

namespace ApiContracts;

public class CustomerDto
{
    public required string UserName { get; set; }
    public required int Id { get; set; }
}
