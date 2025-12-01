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
        var grpcRequest = new GetPendingProductsRequest{};
        try{
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
          catch (Grpc.Core.RpcException ex)
    {
        Console.WriteLine(
            $"[WebAPI] GetPendingProducts gRPC error: " +
            $"Status={ex.Status.StatusCode}, Detail='{ex.Status.Detail}'");

        return StatusCode(StatusCodes.Status502BadGateway, new ProblemDetails
        {
            Title = "Error calling GetPendingProducts gRPC service",
            Detail = ex.Status.Detail,
            Status = StatusCodes.Status502BadGateway
        });
    }
}
    



    private DetailedProductDto toDetailedDto(GetProductResponse response)
    {
var pawnshop     = response.Pawnshop;
    var pawnshopAddr = response.PawnshopAddress;
    var pawnshopPost = response.PawnshopPostal;

    return new DetailedProductDto
    {
        ProductId          = response.Product.Id,
        Price              = response.Product.Price,
        Sold               = response.Product.Sold,
        Condition          = response.Product.Condition,
        ApprovalStatus     = response.Product.ApprovalStatus.ToString(),
        Name               = response.Product.Name,
        PhotoUrl           = response.Product.PhotoUrl,
        Category           = response.Product.Category.ToString(),
        Description        = response.Product.Description,
        SoldByCustomerId   = response.Product.SoldByCustomerId,
        RegisterDate       = response.Product.RegisterDate.ToDateTime(),

        // seller
        SellerId           = response.Customer.Id,
        SellerFirstName    = response.Customer.FirstName,
        SellerLastName     = response.Customer.LastName,
        SellerEmail        = response.Customer.Email,
        SellerPhoneNumber  = response.Customer.PhoneNumber,

        // pawnshop is OPTIONAL
        PawnshopId         = pawnshop?.Id ?? 0,
        PawnshopName       = pawnshop?.Name ?? string.Empty,
        PawnshopAddressId  = pawnshopAddr?.Id ?? 0,
        PawnshopStreetName = pawnshopAddr?.StreetName ?? string.Empty,
        PawnshopSecondaryUnit = pawnshopAddr?.SecondaryUnit,
        PawnshopPostalCode = pawnshopPost?.PostalCode ?? 0,
        PawnshopCity       = pawnshopPost?.City ?? string.Empty
    };
}
}
