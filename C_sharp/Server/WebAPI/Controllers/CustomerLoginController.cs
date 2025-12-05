using ApiContracts.Dtos;
using Grpc.Core;
using Microsoft.AspNetCore.Http.HttpResults;
using Microsoft.AspNetCore.Mvc;
using ReSpawnMarket.SDK.ServiceExceptions;
using ReSpawnMarket.SDK.ServiceInterfaces;

namespace WebAPI.Controllers;

[ApiController]
[Route("/api/customers/login")]
public class CustomerLoginController : ControllerBase
{
    private readonly ICustomerLoginService _customerLoginService;

    public CustomerLoginController(ICustomerLoginService customerLoginService)
    {
        _customerLoginService = customerLoginService;
    }

    [HttpPost]
    [ProducesResponseType(typeof(CustomerLoginResponseDto), StatusCodes.Status200OK)]
    [ProducesResponseType(typeof(ProblemDetails), StatusCodes.Status400BadRequest)]
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
                CanSell = grpcRes.CanSell
            };
            return Ok(api);
        }
        catch (InvalidLoginException ex)
        {
            return NotFound(new ProblemDetails
            {
                Title = "Customer not found",
                Detail = ex.Message,
                Status = StatusCodes.Status404NotFound
            });
        }
        catch (ApplicationException ex)
        {
            return Problem(detail: ex.Message, statusCode: StatusCodes.Status500InternalServerError);
        }
    }

}
