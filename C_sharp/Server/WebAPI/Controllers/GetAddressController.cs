using ApiContracts.Dtos;
using Com.Respawnmarket;
using Microsoft.AspNetCore.Mvc;
using ReSpawnMarket.SDK.ServiceInterfaces;

namespace WebAPI.Controllers;

[Route("api/addresses")]
[ApiController]
public class GetAddressController : ControllerBase
{
    private readonly IGetAddressService _getAddressService;
    public GetAddressController(IGetAddressService getAddressService)
    {
        _getAddressService = getAddressService;
    }

    [HttpGet("pawnshop")]
    public async Task<IActionResult> GetAddressByPawnshopIdAsync(CancellationToken ct)
    {
        var request = new GetAllPawnshopAddressesRequest
        {
        };
        try
        {
            var response = await _getAddressService.GetAllPawnshopAddressesAsync(request, ct);
            var responseDtos = response.Addresses
                .Select(address => new PawnshopAddressDto
                {
                   AddressId = address.Address.Id,
                   StreetName = address.Address.StreetName,
                   SecondaryUnit = address.Address.SecondaryUnit,
                   PostalCode = address.Address.PostalCode,
                   City = address.Postal.City,
                   PawnshopId = address.PawnshopId
                }).ToList() ?? [];
            return Ok(responseDtos);
        }
        catch (KeyNotFoundException ex)
        {
            return NotFound(ex.Message);
        }
        catch (ApplicationException ex)
        {
            return StatusCode(500, $"Internal server error: {ex.Message}");
        }
    }
}
