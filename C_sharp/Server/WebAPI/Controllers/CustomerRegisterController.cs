    using ApiContracts.Dtos;
using Com.Respawnmarket;
using Grpc.Core;
using Microsoft.AspNetCore.Mvc;
using ReSpawnMarket.SDK.ServiceInterfaces;

namespace WebAPI.Controllers;

// TODO : Introduce second address for customer and its dto at some point 
// TODO : handle exceptions for gRPC calls (email already exists, etc.)
[Route("/api/customers")]
[ApiController]
public class CustomerRegisterController : ControllerBase
{
    private readonly IRegisterCustomerService _registerCustomerService;

    public CustomerRegisterController(IRegisterCustomerService registerCustomerService)
    {
        _registerCustomerService = registerCustomerService;
    }


    [HttpPost]
    [ProducesResponseType(typeof(CustomerDto), StatusCodes.Status201Created)]
    [ProducesResponseType(typeof(ProblemDetails), StatusCodes.Status400BadRequest)]
    [ProducesResponseType(typeof(ProblemDetails), StatusCodes.Status502BadGateway)]
    public async Task<IActionResult> RegisterCustomerAsync([FromBody] CreateCustomerDto dto, CancellationToken ct)
    {
        if (!ModelState.IsValid) return ValidationProblem(ModelState);

        var grpcReq = new RegisterCustomerRequest
        {
            FirstName    = dto.FirstName,
            LastName     = dto.LastName,
            Email        = dto.Email,
            Password     = dto.Password,
            PhoneNumber  = dto.PhoneNumber,
            StreetName   = dto.StreetName,
            SecondaryUnit= dto.SecondaryUnit ?? string.Empty,
            PostalCode   = dto.PostalCode,
            City         = dto.City
        };

        try
        {
           var grpcRes = await _registerCustomerService.RegisterCustomerAsync(grpcReq, ct);

           if (grpcRes?.Customer is null || grpcRes.Address is null || grpcRes.Postal is null)
           return StatusCode(502, "Upstream gRPC service returned an incomplete response.");

           var api = new CustomerDto
           {
              Id = grpcRes.Customer.Id,
              FirstName = grpcRes.Customer.FirstName,
              LastName = grpcRes.Customer.LastName,
              Email = grpcRes.Customer.Email,
              PhoneNumber = grpcRes.Customer.PhoneNumber,
              StreetName = grpcRes.Address.StreetName,
              SecondaryUnit = string.IsNullOrWhiteSpace(grpcRes.Address.SecondaryUnit) ? null : grpcRes.Address.SecondaryUnit,
              PostalCode = grpcRes.Postal.PostalCode,
              City = grpcRes.Postal.City,
              CanSell = grpcRes.Customer.CanSell
           };
           return Ok(api);
        }
        catch (ArgumentException ex)
        {
            return BadRequest(ex.Message);
        }
        catch (InvalidOperationException ex)
        {
            return BadRequest(ex.Message);
        }
        catch (RpcException rpcEx)
        {
           // Log the exception details here as needed
           return StatusCode(502, $"Upstream gRPC service error: {rpcEx.Status.Detail}");
        }
    }
    


}