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

    [HttpGet("{id}")]
    public async Task<IActionResult> GetCustomerAsync([FromRoute] int id, CancellationToken cancellationToken)
    {
        try
        {
            var request = new Com.Respawnmarket.GetCustomerRequest
            {
                CustomerId = id
            };
            var response = await _getCustomerService.GetCustomerAsync(request, cancellationToken);
            return Ok(response);
        }
        catch (Exception ex)
        {
            return StatusCode(500, ex.Message);
        }
    }
}
