using ApiContracts;
using Microsoft.AspNetCore.Mvc;

namespace WebAPI.Controllers;

[Route("service/[controller]")] // service/GetCustomerService
[ApiController]
public class GetCustomerServiceController : ControllerBase
{
   private readonly ReSpawnMarket.SDK.ServiceInterfaces.IGetCustomerService _getCustomerService;
    public GetCustomerServiceController(ReSpawnMarket.SDK.ServiceInterfaces.IGetCustomerService getCustomerService)
    {
        _getCustomerService = getCustomerService;
    }

    [HttpGet("{id:int}")]
    public async Task<IActionResult> GetCustomerAsync([FromRoute] int id, CancellationToken ct)
    {
   var grpcRequest = new Com.Respawnmarket.GetCustomerRequest { CustomerId = id };

        var grpcResponse = await _getCustomerService.GetCustomerAsync(grpcRequest, ct);
       if (grpcResponse == null || grpcResponse.Customer == null)
            return NotFound();

        var c = grpcResponse.Customer;

        var addr = grpcResponse.Addresses.Count > 0 ? grpcResponse.Addresses[0] : null;
        var post = grpcResponse.Postals.Count   > 0 ? grpcResponse.Postals[0]   : null;

        var dto = new CustomerDto
        {
            Id            = c.Id,
            FirstName     = c.FirstName,
            LastName      = c.LastName,
            Email         = c.Email,
            PhoneNumber   = c.PhoneNumber,
            StreetName    = addr?.StreetName ?? string.Empty,
            SecondaryUnit = addr?.SecondaryUnit,
            PostalCode    = post?.PostalCode ?? 0,
            City          = post?.City ?? string.Empty
        };

        return Ok(dto);
    }
}
