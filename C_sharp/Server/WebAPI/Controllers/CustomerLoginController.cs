using ApiContracts.Dtos;
using Grpc.Core;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Http.HttpResults;
using Microsoft.AspNetCore.Mvc;
using Microsoft.IdentityModel.Tokens;
using ReSpawnMarket.SDK.ServiceExceptions;
using ReSpawnMarket.SDK.ServiceInterfaces;
using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;
using System.Text;

namespace WebAPI.Controllers;

[ApiController]
[Route("/api/customers")]
public class CustomerLoginController : ControllerBase
{
    private readonly ICustomerLoginService _customerLoginService;
    private IConfiguration config;
    public CustomerLoginController(ICustomerLoginService customerLoginService, IConfiguration config)
    {
        _customerLoginService = customerLoginService;
        this.config = config;
    }

    [HttpPost("login")]
    [ProducesResponseType(typeof(CustomerLoginResponseDto), StatusCodes.Status200OK)]
    [ProducesResponseType(typeof(InvalidLoginException), StatusCodes.Status400BadRequest)]
    public async Task<IActionResult> LoginCustomerAsync([FromBody] CustomerLoginDto dto, CancellationToken ct)
    {
        if (!ModelState.IsValid) return ValidationProblem(ModelState);
        var grpcReq = new Com.Respawnmarket.CustomerLoginRequest
        {
            Email = dto.Email,
            Password = dto.Password
        };
        try
        {
            var grpcRes = await _customerLoginService.LoginCustomerAsync(grpcReq, ct);
            var api = new CustomerLoginResponseDto
            {
                CustomerId = grpcRes.CustomerId,
                FirstName = grpcRes.FirstName,
                LastName = grpcRes.LastName,
                Email = grpcRes.Email,
                PhoneNumber = grpcRes.PhoneNumber,
                CanSell = grpcRes.CanSell,
            };
            var jwtToken = GenerateJwt(api);
            api.JwtToken = jwtToken; // set JWT token
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
            return BadRequest(ex.Message);
        }
        catch (ApplicationException ex)
        {
            return Problem(detail: ex.Message, statusCode: StatusCodes.Status500InternalServerError);
        }
    }

    [HttpPost("logout")]
    [ProducesResponseType(StatusCodes.Status200OK)]
    public IActionResult LogoutCustomer()
    {
        Response.Cookies.Delete("JwtCookie");
        return Ok(new { Message = "Logged out successfully" });
    }

    [Authorize]
    [HttpGet("claims")]
    [ProducesResponseType(StatusCodes.Status401Unauthorized)]
    public IActionResult GetClaims()
    {
        var dto = new CustomerClaimDto
        {
            Subject = User.FindFirst(JwtRegisteredClaimNames.Sub)?.Value ?? "",
            JwtId = User.FindFirst(JwtRegisteredClaimNames.Jti)?.Value ?? "",
            IssuedAt = ToUniXIssueDate(User.FindFirst(JwtRegisteredClaimNames.Iat)?.Value!),
            CustomerId = int.TryParse(User.FindFirst("CustomerId")?.Value, out var id) ? id : 0,
            FirstName = User.FindFirst(ClaimTypes.GivenName)?.Value ?? "",
            LastName = User.FindFirst(ClaimTypes.Surname)?.Value ?? "",
            Email = User.FindFirst(ClaimTypes.Email)?.Value ?? "",
            Role = User.FindFirst(ClaimTypes.Role)?.Value ?? "",
            PhoneNumber = User.FindFirst(ClaimTypes.MobilePhone)?.Value,
            CanSell = User.FindFirst("CanSell")?.Value == "true"
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

    private string GenerateJwt(CustomerLoginResponseDto dto)
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

    private List<Claim> GenerateClaims(CustomerLoginResponseDto dto)
    {
        var claims = new List<Claim>
        {
            new Claim(JwtRegisteredClaimNames.Sub, config["Jwt:Subject"] 
            ?? throw new InvalidOperationException("JWT Subject is not configured")),
            new Claim(JwtRegisteredClaimNames.Jti, Guid.NewGuid().ToString()),
            new Claim(JwtRegisteredClaimNames.Iat, DateTime.UtcNow.ToString()),
            new Claim("CustomerId", dto.CustomerId.ToString()),
            new Claim(ClaimTypes.GivenName, dto.FirstName),
            new Claim(ClaimTypes.Surname, dto.LastName),
            new Claim(ClaimTypes.Email, dto.Email),
            new Claim(ClaimTypes.Role, "Customer")
        };
        if (!string.IsNullOrEmpty(dto.PhoneNumber))
        {
            claims.Add(new Claim(ClaimTypes.MobilePhone, dto.PhoneNumber));
        }
        if (dto.CanSell)
        {
            claims.Add(new Claim("CanSell", "true"));
        }
        return [.. claims];
    }

}
