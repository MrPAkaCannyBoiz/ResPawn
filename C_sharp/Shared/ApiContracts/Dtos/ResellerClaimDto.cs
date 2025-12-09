using System;
using System.Collections.Generic;
using System.Text;

namespace ApiContracts.Dtos;

public class ResellerClaimDto
{
    public string Subject { get; set; } = string.Empty;  // JwtRegisteredClaimNames.Sub
    public string JwtId { get; set; } = string.Empty;    // JwtRegisteredClaimNames.Jti
    public DateTime IssuedAt { get; set; }     // JwtRegisteredClaimNames.Iat
    public int ResellerId { get; set; }
    public string Username { get; set; } = string.Empty;
}
