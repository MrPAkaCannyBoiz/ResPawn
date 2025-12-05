using System;

namespace ApiContracts.Dtos;

public class ResellerLoginDto
{
    public required string Username {get; set;}
    public required string Password {get;set;}
}
