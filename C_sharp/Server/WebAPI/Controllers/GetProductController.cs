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
    [ProducesResponseType(typeof(DetailedProductDto), StatusCodes.Status200OK)]
    [ProducesResponseType(typeof(ProblemDetails), StatusCodes.Status500InternalServerError)]
    public async Task<IActionResult> GetProductAsync(int Id, CancellationToken ct)
    {
        var grpcRequest = new GetProductRequest
        {
            ProductId = Id
        };
        try
        {
            var grpcResponse = await _getProductService.GetProductAsync(grpcRequest, ct);
            var responseDto = toDetailedDto(grpcResponse);
            return Ok(responseDto);
        }
        catch (KeyNotFoundException ex)
        {
            return NotFound(new ProblemDetails
            {
                Title = "Not Found",
                Detail = ex.Message,
                Status = StatusCodes.Status404NotFound
            });
        }
    }

    [HttpGet]
    [ProducesResponseType(typeof(IQueryable<ProductWithFirstImageDto>), StatusCodes.Status200OK)]
    [ProducesResponseType(typeof(List<ProductWithFirstImageDto>), StatusCodes.Status200OK)]
    [ProducesResponseType(typeof(ProblemDetails), StatusCodes.Status500InternalServerError)]
    public async Task<IActionResult> GetAllProductAsync(CancellationToken ct)
    {
        var grpcRequest = new GetAllProductsRequest
        {
        };
        var grpcResponse = await _getProductService.GetAllProductsAsync(grpcRequest, ct);
        var responseDtoList = grpcResponse.Products
            .Select(p => new ProductWithFirstImageDto
            {
                Id = p.Product.Id,
                Price = p.Product.Price,
                Sold = p.Product.Sold,
                Condition = p.Product.Condition,
                ApprovalStatus = p.Product.ApprovalStatus.ToString(),
                Name = p.Product.Name,
                Category = p.Product.Category.ToString(),
                Description = p.Product.Description,
                SoldByCustomerId = p.Product.SoldByCustomerId,
                RegisterDate = p.Product.RegisterDate.ToDateTime(),
                OtherCategory = p.Product.OtherCategory,
                Image = new ImageDto
                {
                    Id = p.FirstImage.Id,
                    Url = p.FirstImage.Url,
                    ProductId = p.FirstImage.ProductId
                }
            })
            .ToList() ?? []; // Return empty list of ProductDto if null
        return Ok(responseDtoList);
    }

    [HttpGet("pending")]
    [ProducesResponseType(typeof(IQueryable<ProductWithFirstImageDto>), StatusCodes.Status200OK)]
    [ProducesResponseType(typeof(List<ProductWithFirstImageDto>), StatusCodes.Status200OK)]
    [ProducesResponseType(typeof(ProblemDetails), StatusCodes.Status500InternalServerError)]
    public async Task<IActionResult> GetPendingProductsAsync(CancellationToken ct)
    {
        var grpcRequest = new GetPendingProductsRequest
        {
        };
        var grpcResponse = await _getProductService.GetPendingProductsAsync(grpcRequest, ct);
        var responseDtoList = grpcResponse.Products
            .Select(p => new ProductWithFirstImageDto
            {
                Id = p.Product.Id,
                Price = p.Product.Price,
                Sold = p.Product.Sold,
                Condition = p.Product.Condition,
                ApprovalStatus = p.Product.ApprovalStatus.ToString(),
                Name = p.Product.Name,
                Category = p.Product.Category.ToString(),
                Description = p.Product.Description,
                SoldByCustomerId = p.Product.SoldByCustomerId,
                RegisterDate = p.Product.RegisterDate.ToDateTime(),
                OtherCategory = p.Product.OtherCategory,
                Image = new ImageDto
                {
                    Id = p.FirstImage.Id,
                    Url = p.FirstImage.Url,
                    ProductId = p.FirstImage.ProductId
                }
            })
            .ToList() ?? []; // Return empty list of ProductDto if null
        return Ok(responseDtoList);
    }

    [HttpGet("available")]
    [ProducesResponseType(typeof(IQueryable<ProductWithFirstImageDto>), StatusCodes.Status200OK)]
    [ProducesResponseType(typeof(List<ProductWithFirstImageDto>), StatusCodes.Status200OK)]
    [ProducesResponseType(typeof(ProblemDetails), StatusCodes.Status500InternalServerError)]
    public async Task<IActionResult> GetAvailableProductsAsync(CancellationToken ct)
    {
        var grpcRequest = new GetAllAvailableProductsRequest
        {
        };
        var grpcResponse = await _getProductService.GetAllAvailableProductsAsync(grpcRequest, ct);
        var responseDtoList = grpcResponse.Products
            .Select(p => new ProductWithFirstImageDto
            {
                Id = p.Product.Id,
                Price = p.Product.Price,
                Sold = p.Product.Sold,
                Condition = p.Product.Condition,
                ApprovalStatus = p.Product.ApprovalStatus.ToString(),
                Name = p.Product.Name,
                Category = p.Product.Category.ToString(),
                Description = p.Product.Description,
                SoldByCustomerId = p.Product.SoldByCustomerId,
                RegisterDate = p.Product.RegisterDate.ToDateTime(),
                OtherCategory = p.Product.OtherCategory,
                Image = new ImageDto
                {
                    Id = p.FirstImage.Id,
                    Url = p.FirstImage.Url,
                    ProductId = p.FirstImage.ProductId
                }
            })
            .ToList() ?? []; // Return empty list of ProductDto if null
        return Ok(responseDtoList);
    }

   
    private DetailedProductDto toDetailedDto(GetProductResponse response)
    {
        var images = response.Images.Select(img => new ImageDto()
        {
            Id = img.Id,
            Url = img.Url,
            ProductId = img.ProductId
        }).ToList();

        return new DetailedProductDto
        {
            ProductId = response.Product.Id,
            Price = response.Product.Price,
            Sold = response.Product.Sold,
            Condition = response.Product.Condition,
            ApprovalStatus = response.Product.ApprovalStatus.ToString(),
            Name = response.Product.Name,
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
            PawnshopCity = response.PawnshopPostal.City,
            Images = images
        };
    }
}
