using ApiContracts;
using ApiContracts.Dtos;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using Microsoft.IdentityModel.Tokens;
using ReSpawnMarket.SDK.ServiceExceptions;
using ReSpawnMarket.SDK.ServiceInterfaces;
using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;
using System.Text;

namespace WebAPI.Controllers;

[Route("api/reseller")]
[ApiController]
public class ResellerLoginController : ControllerBase
{
    private readonly IResellerLoginService resellerLoginService;
    private IConfiguration config;

    public ResellerLoginController(IResellerLoginService resellerLoginService, IConfiguration config)
    {
        this.resellerLoginService = resellerLoginService;
        this.config = config;
    }
    [HttpPost("login")]
    [ProducesResponseType(typeof(ResellerLoginResponseDto), StatusCodes.Status200OK)]
    [ProducesResponseType(typeof(ProblemDetails), StatusCodes.Status400BadRequest)]
    public async Task<IActionResult> LoginResellerAsync([FromBody] ResellerLoginDto dto, CancellationToken ct)
    {
        if (!ModelState.IsValid) return ValidationProblem(ModelState);
        var grpcReq = new Com.Respawnmarket.ResellerLoginRequest
        {
            Username = dto.Username,
            Password = dto.Password
        };
        try
        {
            var grpcRes = await resellerLoginService.LoginResellerAsync(grpcReq, ct);
            var api = new ResellerLoginResponseDto
            {
                Id = grpcRes.Id,
                Username = grpcRes.Username,
                Jwt = string.Empty // will be set later
            };
            var jwtToken = GenerateJwt(api);
            api.Jwt = jwtToken; // set JWT token
            Response.Cookies.Append("JwtCookie", jwtToken, new CookieOptions
            {
                HttpOnly = true,
                Secure = true,
                SameSite = SameSiteMode.Strict,
                Expires = DateTimeOffset.UtcNow.AddMinutes(60)
            });
            return Ok(api);
        }
        catch (InvalidLoginException ex)
        {
            return NotFound(new ProblemDetails
            {
                Title = "Reseller not found",
                Detail = ex.Message,
                Status = StatusCodes.Status404NotFound
            });
        }
        catch (ApplicationException ex)
        {
            return Problem(detail: ex.Message, statusCode: StatusCodes.Status500InternalServerError);
        }
    }

    [HttpPost("logout")]
    [ProducesResponseType(StatusCodes.Status200OK)]
    public IActionResult LogoutReseller()
    {
        Response.Cookies.Delete("JwtCookie");
        return Ok(new { Message = "Logged out successfully" });
    }

    private string GenerateJwt(ResellerLoginResponseDto dto)
    {
        JwtSecurityTokenHandler tokenHandler = new();
        var key = Encoding.ASCII.GetBytes(config["Jwt:Key"]
            ?? throw new InvalidLoginException("Jwt key is not configured"));

        List<Claim> claims = GenerateClaims(dto);

        var tokenDescriptor = new SecurityTokenDescriptor
        {
            Subject = new ClaimsIdentity(claims),
            Expires = DateTime.UtcNow.AddMinutes(60),
            SigningCredentials = new SigningCredentials(new SymmetricSecurityKey(key),
            SecurityAlgorithms.HmacSha256Signature),
            Issuer = config["Jwt:Issuer"],
            Audience = config["Jwt:Audience"]
        };
        var token = tokenHandler.CreateToken(tokenDescriptor);
        return tokenHandler.WriteToken(token);
    }
    private List<Claim> GenerateClaims(ResellerLoginResponseDto dto)
    {
        var claims = new List<Claim>
        {
            new Claim(JwtRegisteredClaimNames.Sub, config["Jwt:Subject"]
            ?? throw new InvalidOperationException("JWT Subject is not configured")),
            new Claim(JwtRegisteredClaimNames.Jti, Guid.NewGuid().ToString()),
            new Claim(JwtRegisteredClaimNames.Iat, DateTime.UtcNow.ToString()),
            new Claim("ResellerId", dto.Id.ToString()),
            new Claim("Username", dto.Username),
            new Claim(ClaimTypes.Role, "Reseller")
        };
      
        return [.. claims];
    }

    [Authorize]
    [HttpGet("claims")]
    [ProducesResponseType(StatusCodes.Status401Unauthorized)]
    public IActionResult GetClaims()
    {
        var dto = new ResellerClaimDto
        {
            Subject = User.FindFirst(JwtRegisteredClaimNames.Sub)?.Value ?? "",
            JwtId = User.FindFirst(JwtRegisteredClaimNames.Jti)?.Value ?? "",
            IssuedAt = ToUniXIssueDate(User.FindFirst(JwtRegisteredClaimNames.Iat)?.Value!),
            ResellerId = int.TryParse(User.FindFirst("ResellerId")?.Value, out var id) ? id : 0,
            Username = User.FindFirst("Username")?.Value ?? ""
        };
        foreach (var claim in User.Claims)
        {
            Console.WriteLine($"Type: {claim.Type}, Value: {claim.Value}");
        }
        return Ok(dto);
    }

    private DateTime ToUniXIssueDate(string iatClaim)
    {
        DateTime issuedAt = default;
        if (long.TryParse(iatClaim, out var unixSeconds))
        {
            issuedAt = DateTimeOffset.FromUnixTimeSeconds(unixSeconds).UtcDateTime;
        }
        else if (DateTime.TryParse(iatClaim, out var dt))
        {
            issuedAt = dt;
        }
        return issuedAt;
    }
}
