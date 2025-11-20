using ApiContracts.Dtos;
using Com.Respawnmarket;
using Microsoft.AspNetCore.Mvc;
using ReSpawnMarket.SDK.ServiceInterfaces;

namespace WebAPI.Controllers;

[Route("api/products")]
[ApiController]
public class UploadProductController : ControllerBase
{
    private readonly IUploadProductService _uploadProductService;

    public UploadProductController(IUploadProductService uploadProductService)
    {
        _uploadProductService = uploadProductService;
    }

    [HttpPost("customers/{customerId}")]
    // handle the exception globally
    [ProducesResponseType(typeof(UploadProductDto), StatusCodes.Status200OK)]
    [ProducesResponseType(typeof(ProblemDetails), StatusCodes.Status400BadRequest)]
    [ProducesResponseType(typeof(ProblemDetails), StatusCodes.Status502BadGateway)]
    public async Task<IActionResult> UploadProductAsync([FromBody] UploadProductDto dto, 
        [FromRoute] int customerId ,CancellationToken ct)
    {
        var grpcRequest = new UploadProductRequest
        {
            Price = dto.Price,
            Condition = dto.Condition,
            Description = dto.Description,
            Name = dto.Name,
            PhotoUrl = dto.PhotoUrl,
            Category = (Category)dto.Category,
            OtherCategory = dto.OtherCategory ?? string.Empty,
            SoldByCustomerId = customerId
        };

        var grpcResponse = await _uploadProductService.UploadProductAsync(grpcRequest, ct);
        if (grpcRequest.Category == Category.Unspecified)
        {
            return StatusCode(502, new ProblemDetails
            {
                Title = "Bad Gateway",
                Detail = "The upstream service returned an invalid category.",
                Status = StatusCodes.Status502BadGateway
            });
        }

        ProductDto responseDto = new()
        {
           Id = grpcResponse.Product.Id,
           Price = grpcResponse.Product.Price,
           Sold = grpcResponse.Product.Sold,
           Condition = grpcResponse.Product.Condition,
           ApprovalStatus = (ApiContracts.Dtos.Enums.ApprovalStatus) grpcResponse.Product.ApprovalStatus,
           Name = grpcResponse.Product.Name,
           PhotoUrl = grpcResponse.Product.PhotoUrl,
           Category = (ApiContracts.Dtos.Enums.Category) grpcResponse.Product.Category,
           Description = grpcResponse.Product.Description,
           SoldByCustomerId = grpcResponse.Product.SoldByCustomerId,
           RegisterDate = grpcResponse.Product.RegisterDate.ToDateTime()
        };
        return Ok(responseDto);
    }
}
