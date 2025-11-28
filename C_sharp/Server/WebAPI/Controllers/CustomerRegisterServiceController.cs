using ApiContracts.Dtos;
using Com.Respawnmarket;
using Grpc.Core;
using Microsoft.AspNetCore.Mvc;
using ReSpawnMarket.SDK.ServiceInterfaces;

namespace WebAPI.Controllers;

// TODO : Introduce second address for customer and its dto at some point 
// TODO : handle exceptions for gRPC calls (email already exists, etc.)
[Route("api/customers")]
[ApiController]
public class CustomerRegisterServiceController : ControllerBase
{
    private readonly IRegisterCustomerService _registerCustomerService;

    private readonly IGetCustomerService _getCustomerService;

    public CustomerRegisterServiceController(IRegisterCustomerService registerCustomerService, IGetCustomerService getCustomerService)
    {
        _registerCustomerService = registerCustomerService;
        _getCustomerService = getCustomerService;
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

    var grpcRes = await _registerCustomerService.RegisterCustomerAsync(grpcReq, ct);

    if (grpcRes?.Customer is null || grpcRes.Address is null || grpcRes.Postal is null)
        return StatusCode(502, "Upstream gRPC service returned an incomplete response.");

    var api = new CustomerDto
    {
        Id            = grpcRes.Customer.Id,
        FirstName     = grpcRes.Customer.FirstName,
        LastName      = grpcRes.Customer.LastName,
        Email         = grpcRes.Customer.Email,
        PhoneNumber   = grpcRes.Customer.PhoneNumber,
        StreetName    = grpcRes.Address.StreetName,
        SecondaryUnit = string.IsNullOrWhiteSpace(grpcRes.Address.SecondaryUnit) ? null : grpcRes.Address.SecondaryUnit,
        PostalCode    = grpcRes.Postal.PostalCode,
        City          = grpcRes.Postal.City
    };

    // Defensive: ensure we actually got a DB-generated id
    if (api.Id <= 0) return StatusCode(502, "Upstream gRPC service did not return a valid customer id.");

    // Use the named route
    return CreatedAtRoute("GetCustomerById", new { id = api.Id }, api);
}
    

    // READ
    [HttpGet("{id:int}", Name = "GetCustomerById")]
    public async Task<ActionResult<CustomerDto>> GetByIdAsync(int id, CancellationToken ct)
    {
        var grpcReq = new GetCustomerRequest { CustomerId = id };
        var grpcRes = await _getCustomerService.GetCustomerAsync(grpcReq, ct);

        var dto = new CustomerDto
        {
            Id = grpcRes.Id,
            FirstName = grpcRes.FirstName,
            LastName = grpcRes.LastName,
            Email = grpcRes.Email,
            PhoneNumber = grpcRes.PhoneNumber,
            StreetName = grpcRes.Addresses?.FirstOrDefault()?.StreetName ?? "",
            SecondaryUnit = grpcRes.Addresses?.FirstOrDefault()?.SecondaryUnit,
            PostalCode = grpcRes.Postals?.FirstOrDefault()?.PostalCode ?? 0,
            City = grpcRes.Postals?.FirstOrDefault()?.City ?? ""
        };

        return Ok(dto);
    }
}