using ApiContracts.Dtos;
using Microsoft.AspNetCore.Mvc;
using ReSpawnMarket.SDK.ServiceInterfaces;

namespace WebAPI.Controllers;

[ApiController]
[Route("/api/customer/inspection")]
public class CustomerInspectionController : ControllerBase
{
    private readonly ICustomerInspectionService _customerInspectionService;
    public CustomerInspectionController(ICustomerInspectionService customerInspectionService)
    {
        _customerInspectionService = customerInspectionService;
    }

    [HttpPatch("{customerId}")]
    [ProducesResponseType(typeof(EnableSellDto), StatusCodes.Status200OK)]
    [ProducesResponseType(typeof(ProblemDetails), StatusCodes.Status400BadRequest)]
    [ProducesResponseType(typeof(KeyNotFoundException), StatusCodes.Status404NotFound)]
    [ProducesResponseType(typeof(ApplicationException), StatusCodes.Status500InternalServerError)]
    public async Task<IActionResult> SetCanSellAsync([FromBody] EnableSellDto dto, 
        int customerId, CancellationToken ct)
    {
        var grpcReq = new Com.Respawnmarket.EnableSellingRequest
        {
            CustomerId = customerId,
            CanSell = dto.CanSell
        };
        try
        {
            var grpcRes = await _customerInspectionService.SetCanSellAsync(grpcReq, ct);
            var resultDto = new EnableSellDto
            {
                CustomerId = grpcRes.CustomerId,
                CanSell = grpcRes.CanSell
            };
            return Ok(resultDto);
        }
        catch (KeyNotFoundException ex)
        {
            return NotFound(ex.Message);
        }
        catch (ApplicationException ex)
        {
            return Problem(detail: ex.Message, statusCode: StatusCodes.Status500InternalServerError);
        }
    }
}
