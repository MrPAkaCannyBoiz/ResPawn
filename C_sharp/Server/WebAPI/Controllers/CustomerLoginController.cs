using ApiContracts.Dtos;
using Grpc.Core;
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
[Route("/api/customers/login")]
public class CustomerLoginController : ControllerBase
{
    private readonly ICustomerLoginService _customerLoginService;
    private IConfiguration config;
    public CustomerLoginController(ICustomerLoginService customerLoginService, IConfiguration config)
    {
        _customerLoginService = customerLoginService;
        this.config = config;
    }

    [HttpPost]
    [ProducesResponseType(typeof(CustomerLoginResponseDto), StatusCodes.Status200OK)]
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
            var jwtToken = GenerateJwt(new CustomerLoginResponseDto
            {
                CustomerId = grpcRes.CustomerId,
                FirstName = grpcRes.FirstName,
                LastName = grpcRes.LastName,
                Email = grpcRes.Email,
                PhoneNumber = grpcRes.PhoneNumber,
                CanSell = grpcRes.CanSell
            });
            var api = new CustomerLoginResponseDto
            {
                CustomerId = grpcRes.CustomerId,
                FirstName = grpcRes.FirstName,
                LastName = grpcRes.LastName,
                Email = grpcRes.Email,
                PhoneNumber = grpcRes.PhoneNumber,
                CanSell = grpcRes.CanSell,
                JwtToken = jwtToken
            };
            return Ok(api);
        }
        catch (InvalidLoginException ex)
        {
            return NotFound(new ProblemDetails
            {
                Title = "Invalid Email or Password",
                Detail = ex.Message,
                Status = StatusCodes.Status404NotFound
            });
        }
        catch (ApplicationException ex)
        {
            return Problem(detail: ex.Message, statusCode: StatusCodes.Status500InternalServerError);
        }
    }

    private string GenerateJwt(CustomerLoginResponseDto dto)
    {
        JwtSecurityTokenHandler tokenHandler = new();
        var key = Encoding.ASCII.GetBytes(config["Jwt:Key"] ?? "");

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
            new Claim(JwtRegisteredClaimNames.Sub, config["Jwt:Subject"] ?? ""),
            new Claim(JwtRegisteredClaimNames.Jti, Guid.NewGuid().ToString()),
            new Claim(JwtRegisteredClaimNames.Iat, DateTime.UtcNow.ToString()),
            new Claim(ClaimTypes.NameIdentifier, dto.CustomerId.ToString()),
            new Claim(ClaimTypes.GivenName, dto.FirstName),
            new Claim(ClaimTypes.Surname, dto.LastName),
            new Claim(ClaimTypes.Email, dto.Email)
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
