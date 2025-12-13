using System;
using System.Collections.Generic;

namespace ApiContracts.Dtos;

public class CustomerClaimDto
{
    public string Subject { get; set; } = string.Empty;  // JwtRegisteredClaimNames.Sub
    public string JwtId { get; set; } = string.Empty;    // JwtRegisteredClaimNames.Jti
    public DateTime IssuedAt { get; set; }     // JwtRegisteredClaimNames.Iat
    public int CustomerId { get; set; }       // ClaimTypes.NameIdentifier
    public string FirstName { get; set; } = string.Empty;  // ClaimTypes.GivenName
    public string LastName { get; set; } = string.Empty; // ClaimTypes.Surname
    public string Email { get; set; } = string.Empty;  // ClaimTypes.Email
    public string Role { get; set; } = string.Empty;   // ClaimTypes.Role
    public string? PhoneNumber { get; set; }    // ClaimTypes.MobilePhone (nullable)
    public bool CanSell { get; set; }     // "CanSell" claim

}
