using ApiContracts.Dtos;
using Microsoft.IdentityModel.JsonWebTokens;
using System.Security.Claims;

namespace ApiContracts.ClaimFactories;

public static class ResellerClaimsExtention
{
    public static IEnumerable<Claim> ToClaims(ResellerClaimDto dto)
    {
        var claims = new List<Claim>
        {
            new Claim(JwtRegisteredClaimNames.Sub, dto.Subject),
            new Claim(JwtRegisteredClaimNames.Jti, dto.JwtId),
            new Claim(JwtRegisteredClaimNames.Iat, dto.IssuedAt.ToString("o")),
            new Claim("ResellerId", dto.ResellerId.ToString()),
            new Claim("Username", dto.Username),
            new Claim(ClaimTypes.Role, "Reseller")
        };
        return claims;
    }
}
