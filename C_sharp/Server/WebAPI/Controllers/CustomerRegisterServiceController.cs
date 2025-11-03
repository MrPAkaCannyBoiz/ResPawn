using Microsoft.AspNetCore.Mvc;
using ReSpawnMarket.SDK.ServiceInterfaces;

namespace WebAPI.Controllers;

[Route("api/[controller]")] // api/CustomerRegisterService
[ApiController]
public class CustomerRegisterServiceController : ControllerBase
{
   private readonly IRegisterCustomerService _registerCustomerService;

    public CustomerRegisterServiceController(IRegisterCustomerService registerCustomerService)
    {
        _registerCustomerService = registerCustomerService;
    }

    //only service we have here is create customer
    [HttpPost("RegisterCustomer")]
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
