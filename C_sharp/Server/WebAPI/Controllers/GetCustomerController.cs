using ApiContracts.Dtos;
using Com.Respawnmarket;
using Microsoft.AspNetCore.Mvc;
using ReSpawnMarket.SDK.ServiceInterfaces;

namespace WebAPI.Controllers;

// TODO : Introduce second address for customer and its dto at some point 
[Route("/api/customers")]
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
    //instead of try catch in controller, we use ExceptionMiddleware to catch unhandled exceptions globally
    [ProducesResponseType(typeof(KeyNotFoundException), StatusCodes.Status404NotFound)]
    [ProducesResponseType(typeof(ApplicationException), StatusCodes.Status502BadGateway)]
    [ProducesErrorResponseType(typeof(Exception))]
    public async Task<IActionResult> GetCustomerAsync(int customerId, CancellationToken ct)
    {
        var grpcReq = new GetCustomerRequest { CustomerId = customerId };
        var grpcRes = await _getCustomerService.GetCustomerAsync(grpcReq, ct);
        var dto = grpcRes.Customer;
        var customerDto = new CustomerDto
        {
            Id = dto.Id,
            FirstName = dto.FirstName,
            LastName = dto.LastName,
            Email = dto.Email,
            PhoneNumber = dto.PhoneNumber,
            StreetName = dto.Addresses?.FirstOrDefault()?.StreetName ?? "",
            SecondaryUnit = dto.Addresses?.FirstOrDefault()?.SecondaryUnit ?? "",
            PostalCode = dto.Postals?.FirstOrDefault()?.PostalCode ?? 0,
            City = dto.Postals?.FirstOrDefault()?.City ?? "",
            CanSell = dto.CanSell
        };
        return Ok(customerDto);
    }

    [HttpGet]
    [ProducesResponseType(typeof(List<CustomerDto>), StatusCodes.Status200OK)]
    [ProducesResponseType(typeof(KeyNotFoundException), StatusCodes.Status404NotFound)]
    [ProducesResponseType(typeof(ApplicationException), StatusCodes.Status502BadGateway)]
    [ProducesErrorResponseType(typeof(Exception))]
    public async Task<IActionResult> GetAllCustomersAsync(CancellationToken ct)
    {
        var grpcReq = new GetAllCustomersRequest { };
        var grpcRes = await _getCustomerService.GetAllCustomerAsync(grpcReq,  ct);
        var dtoList = grpcRes.Customers.Select(grpcCustomer => new CustomerDto
        {
            Id = grpcCustomer.Id,
            FirstName = grpcCustomer.FirstName,
            LastName = grpcCustomer.LastName,
            Email = grpcCustomer.Email,
            PhoneNumber = grpcCustomer.PhoneNumber,
            StreetName = grpcCustomer.Addresses?.FirstOrDefault()?.StreetName ?? "",
            SecondaryUnit = grpcCustomer.Addresses?.FirstOrDefault()?.SecondaryUnit ?? "",
            PostalCode = grpcCustomer.Postals?.FirstOrDefault()?.PostalCode ?? 0,
            City = grpcCustomer.Postals?.FirstOrDefault()?.City ?? "",
            CanSell = grpcCustomer.CanSell
        }).ToList() ?? []; // in case of null, return empty list
        return Ok(dtoList);
    }


}