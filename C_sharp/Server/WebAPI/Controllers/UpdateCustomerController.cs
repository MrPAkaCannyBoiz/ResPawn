using ApiContracts.Dtos;
using Com.Respawnmarket;
using Grpc.Core;
using Microsoft.AspNetCore.Mvc;
using ReSpawnMarket.SDK.ServiceExceptions;
using ReSpawnMarket.SDK.ServiceInterfaces;

namespace WebAPI.Controllers;

// TODO : Introduce second address for customer and its dto at some point 
// TODO : handle exceptions for gRPC calls like the other class does
[Route("/api/customers")]
[ApiController]
public class UpdateCustomerController : ControllerBase
{
    private readonly IUpdateCustomerService _updateCustomerService;

    public UpdateCustomerController(IUpdateCustomerService updateCustomerService)
    {
        _updateCustomerService = updateCustomerService;
    }


    [HttpPatch("{customerId}")] //partial update: use PATCH

    [ProducesResponseType(typeof(void), StatusCodes.Status204NoContent)]
    [ProducesResponseType(typeof(CustomerDto), StatusCodes.Status200OK)]
    [ProducesResponseType(typeof(ProblemDetails), StatusCodes.Status400BadRequest)]
    [ProducesResponseType(typeof(ProblemDetails), StatusCodes.Status502BadGateway)]
    public async Task<IActionResult> UpdateCustomerAsync([FromBody] UpdateCustomerDto dto, int customerId, CancellationToken ct)
    {
        var grpcReq = new UpdateCustomerRequest
        {
            CustomerId = customerId,
            FirstName = dto.FirstName ?? "",
            LastName = dto.LastName ?? "",
            Email = dto.Email ?? "",
            PhoneNumber = dto.PhoneNumber ?? "",
            StreetName = dto.StreetName ?? "",
            SecondaryUnit = dto.SecondaryUnit ?? "",
            PostalCode = dto.PostalCode,
            City = dto.City ?? ""
        };
        var customer = new CustomerDto() { FirstName = "", LastName = "", Id = 0};
        try
        {
            var grpcRes = await _updateCustomerService.UpdateCustomerAsync(grpcReq, ct);
            
            var returnDto = new CustomerDto
            {
                Id = grpcRes.Id,
                FirstName = grpcRes.FirstName,
                LastName = grpcRes.LastName,
                Email = grpcRes.Email,
                PhoneNumber = grpcRes.PhoneNumber,
                StreetName = grpcRes.Addresses?.FirstOrDefault()?.StreetName ?? "",
                SecondaryUnit = grpcRes.Addresses?.FirstOrDefault()?.SecondaryUnit ?? "",
                PostalCode = grpcRes.Postals?.FirstOrDefault()?.PostalCode ?? 0,
                City = grpcRes.Postals?.FirstOrDefault()?.City ?? ""
            };
            customer = returnDto;
        }
        catch (UpdateCustomerException ex)
        {
            return BadRequest(ex.Message);
        }
        catch (KeyNotFoundException ex)
        {
            return NotFound(ex.Message);
        }
        catch (ApplicationException ex)
        {
            return StatusCode(StatusCodes.Status502BadGateway, ex.Message);
        }
        return Ok(customer);
    }
}
