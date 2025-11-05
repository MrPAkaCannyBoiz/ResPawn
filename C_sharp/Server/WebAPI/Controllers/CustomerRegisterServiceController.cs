using Microsoft.AspNetCore.Mvc;
using ReSpawnMarket.SDK.ServiceInterfaces;

namespace WebAPI.Controllers;

[Route("api/customers")]
[ApiController]
public class CustomerRegisterServiceController : ControllerBase
{
   private readonly IRegisterCustomerService _registerCustomerService;

    public CustomerRegisterServiceController(IRegisterCustomerService registerCustomerService)
    {
        _registerCustomerService = registerCustomerService;
    }

    //only service we have here is create customer
    [HttpPost]
    public async Task<IActionResult> RegisterCustomerAsync([FromBody] Com.Respawnmarket.RegisterCustomerRequest request
        , CancellationToken cancellationToken)
    {
        try
        {
            var response = await _registerCustomerService.RegisterCustomerAsync(request, cancellationToken);
            return Ok(response);
        }
        catch (Exception ex)
        {
            return StatusCode(500, ex.Message);
        }
    }

}
