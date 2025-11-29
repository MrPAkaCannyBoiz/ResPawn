using ApiContracts.Dtos;
using Com.Respawnmarket;
using Microsoft.AspNetCore.Mvc;
using ReSpawnMarket.SDK.ServiceInterfaces;

namespace WebAPI.Controllers;

[ApiController]
[Route("products")]
public class GetProductController: ControllerBase
{
    private readonly IGetProductService _getProductService;

    public GetProductController(IGetProductService getProductService)
    {
        _getProductService = getProductService;
    }

    [HttpGet("{Id}")]
    [ProducesResponseType(typeof(ProductDto), StatusCodes.Status200OK)]
    [ProducesResponseType(typeof(ProblemDetails), StatusCodes.Status400BadRequest)]
    public async Task<IActionResult> GetProductAsync(int Id, CancellationToken ct)
    {
        var grpcRequest = new GetProductRequest
        {
            ProductId = Id
        };
        var grpcResponse = await _getProductService.GetProductAsync(grpcRequest, ct);
        if (grpcResponse.Product is null)
        {
            return NotFound(new ProblemDetails
            {
                Title = "Not Found",
                Detail = $"Product with ID {Id} was not found.",
                Status = StatusCodes.Status404NotFound
            });
        }
        var responseDto = toDetailedDto(grpcResponse);
        return Ok(responseDto);
    }

    [HttpGet]
    [ProducesResponseType(typeof(List<ProductDto>), StatusCodes.Status200OK)]
    [ProducesResponseType(typeof(ProblemDetails), StatusCodes.Status400BadRequest)]
    public async Task<IActionResult> GetAllProductAsync(CancellationToken ct)
    {
        var grpcRequest = new GetAllProductsRequest
        {
        };
        var grpcResponse = await _getProductService.GetAllProductsAsync(grpcRequest, ct);
        var responseDtoList = grpcResponse.Products
            .Select(p => new ProductDto
            {
                Id = p.Id,
                Price = p.Price,
                Sold = p.Sold,
                Condition = p.Condition,
                ApprovalStatus = p.ApprovalStatus.ToString(),
                Name = p.Name,
                PhotoUrl = p.PhotoUrl,
                Category = p.Category.ToString(),
                Description = p.Description,
                SoldByCustomerId = p.SoldByCustomerId,
                RegisterDate = p.RegisterDate.ToDateTime()
            })
            .ToList() ?? []; // Return empty list of ProductDto if null
        return Ok(responseDtoList);
    }

    [HttpGet("pending")]
    [ProducesResponseType(typeof(List<ProductDto>), StatusCodes.Status200OK)]
    [ProducesResponseType(typeof(ProblemDetails), StatusCodes.Status400BadRequest)]
    public async Task<IActionResult> GetPendingProductsAsync(CancellationToken ct)
    {
        var grpcRequest = new GetPendingProductsRequest
        {
        };
        var grpcResponse = await _getProductService.GetPendingProductsAsync(grpcRequest, ct);
        var responseDtoList = grpcResponse.Products
            .Select(p => new ProductDto
            {
                Id = p.Id,
                Price = p.Price,
                Sold = p.Sold,
                Condition = p.Condition,
                ApprovalStatus = p.ApprovalStatus.ToString(),
                Name = p.Name,
                PhotoUrl = p.PhotoUrl,
                Category = p.Category.ToString(),
                Description = p.Description,
                SoldByCustomerId = p.SoldByCustomerId,
                RegisterDate = p.RegisterDate.ToDateTime()
            })
            .ToList() ?? []; // Return empty list of ProductDto if null
        return Ok(responseDtoList);
    }


    private DetailedProductDto toDetailedDto(GetProductResponse response)
    {
        return new DetailedProductDto
        {
            ProductId = response.Product.Id,
            Price = response.Product.Price,
            Sold = response.Product.Sold,
            Condition = response.Product.Condition,
            ApprovalStatus = response.Product.ApprovalStatus.ToString(),
            Name = response.Product.Name,
            PhotoUrl = response.Product.PhotoUrl,
            Category = response.Product.Category.ToString(),
            Description = response.Product.Description,
            SoldByCustomerId = response.Product.SoldByCustomerId,
            RegisterDate = response.Product.RegisterDate.ToDateTime(),
            SellerId = response.Product.SoldByCustomerId,
            SellerFirstName = response.Customer.FirstName,
            SellerLastName = response.Customer.LastName,
            SellerEmail = response.Customer.Email,
            SellerPhoneNumber = response.Customer.PhoneNumber,
            PawnshopId = response.Pawnshop.Id,
            PawnshopName = response.Pawnshop.Name,
            PawnshopAddressId = response.Pawnshop.AddressId,
            PawnshopStreetName = response.PawnshopAddress.StreetName,
            PawnshopSecondaryUnit = response.PawnshopAddress.SecondaryUnit,
            PawnshopPostalCode = response.PawnshopAddress.PostalCode,
            PawnshopCity = response.PawnshopPostal.City
        };
    }
}
