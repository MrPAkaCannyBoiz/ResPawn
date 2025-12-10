using ApiContracts.Dtos;
using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;

namespace WebAPI.ClaimFactoriesExtension;

public static class CustomerClaimsExtension
{
    public static IEnumerable<Claim> ToClaims(CustomerClaimDto dto)
    {
        var claims = new List<Claim>
        {
            new Claim(JwtRegisteredClaimNames.Sub, dto.Subject),
            new Claim(JwtRegisteredClaimNames.Jti, dto.JwtId),
            new Claim(JwtRegisteredClaimNames.Iat, dto.IssuedAt.ToString("o")),
            new Claim("CustomerId", dto.CustomerId.ToString()),
            new Claim(ClaimTypes.GivenName, dto.FirstName),
            new Claim(ClaimTypes.Surname, dto.LastName),
            new Claim(ClaimTypes.Email, dto.Email),
            new Claim(ClaimTypes.Role, dto.Role)
        };
        if (!string.IsNullOrEmpty(dto.PhoneNumber))
            claims.Add(new Claim(ClaimTypes.MobilePhone, dto.PhoneNumber));
        claims.Add(new Claim("CanSell", dto.CanSell ? "true" : "false"));
        return claims;
    }
}
