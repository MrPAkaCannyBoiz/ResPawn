using ApiContracts.Dtos;
using Com.Respawnmarket;
using Microsoft.AspNetCore.Mvc;
using ReSpawnMarket.SDK.ServiceInterfaces;

namespace WebAPI.Controllers;

// TODO : Introduce second address for customer and its dto at some point 
[Route("api/customers")]
[ApiController]
public class GetCustomerController : ControllerBase
{
    private readonly IGetCustomerService _getCustomerService;

    public GetCustomerController(IGetCustomerService getCustomerService)
    {
        _getCustomerService = getCustomerService;
    }

    [HttpGet("{customerId}")]
    [ProducesResponseType(typeof(CustomerDto), StatusCodes.Status200OK)]
    [ProducesResponseType(typeof(ProblemDetails), StatusCodes.Status400BadRequest)]
    [ProducesResponseType(typeof(ProblemDetails), StatusCodes.Status502BadGateway)]
    [ProducesErrorResponseType(typeof(ApplicationException))]
    public async Task<IActionResult> GetCustomerAsync(int customerId, CancellationToken ct)
    {
        var grpcReq = new GetCustomerRequest
        {
            CustomerId = customerId
        };

        try
        {
            var grpcRes = await _getCustomerService.GetCustomerAsync(grpcReq, ct);
            var dto = new CustomerDto
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
            return Ok(dto);
        }
        catch (ApplicationException ex)
        {
            return BadRequest(ex.Message);
        }
    }
}