using ApiContracts.Dtos;
using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;

namespace WebAPI.ClaimFactoriesExtensions
{
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
}
