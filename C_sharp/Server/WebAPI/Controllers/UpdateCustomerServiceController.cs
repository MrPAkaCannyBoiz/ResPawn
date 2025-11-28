using ApiContracts.Dtos;
using Com.Respawnmarket;
using Grpc.Core;
using Microsoft.AspNetCore.Mvc;
using ReSpawnMarket.SDK.ServiceInterfaces;

namespace WebAPI.Controllers;

// TODO : Introduce second address for customer and its dto at some point 
[Route("api/customers")]
[ApiController]
public class UpdateCustomerServiceController : ControllerBase
{
    private readonly IUpdateCustomerService _updateCustomerService;

    public UpdateCustomerServiceController(IUpdateCustomerService updateCustomerService)
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
            Password = dto.Password ?? "",
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
            if (grpcRes?.Customer is null)
            {
                return NotFound();
            }
            var returnDto = new CustomerDto
            {
                Id = grpcRes.Customer.Id,
                FirstName = grpcRes.Customer.FirstName,
                LastName = grpcRes.Customer.LastName,
                Email = grpcRes.Customer.Email,
                PhoneNumber = grpcRes.Customer.PhoneNumber,
                StreetName = grpcRes.Addresses?.FirstOrDefault()?.StreetName ?? "",
                SecondaryUnit = grpcRes.Addresses?.FirstOrDefault()?.SecondaryUnit ?? "",
                PostalCode = grpcRes.Postals?.FirstOrDefault()?.PostalCode ?? 0,
                City = grpcRes.Postals?.FirstOrDefault()?.City ?? ""
            };
            customer = returnDto;
        }
        catch (RpcException ex) when (ex.StatusCode == Grpc.Core.StatusCode.AlreadyExists)
        {
            return Conflict(new ProblemDetails
            {
                Title = "Email Already Exists",
                Detail = ex.Status.Detail,
                Status = StatusCodes.Status409Conflict
            });
        }
        return Ok(customer);
    }
}
