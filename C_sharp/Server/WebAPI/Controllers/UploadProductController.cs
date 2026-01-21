using ApiContracts.Dtos;
using Com.Respawnmarket;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using ReSpawnMarket.SDK.ServiceExceptions;
using ReSpawnMarket.SDK.ServiceInterfaces;

namespace WebAPI.Controllers;

[Route("/api/products")]
[ApiController]
public class UploadProductController : ControllerBase
{
    private readonly IUploadProductService _uploadProductService;

    public UploadProductController(IUploadProductService uploadProductService)
    {
        _uploadProductService = uploadProductService;
    }

    [Authorize]
    [HttpPost("customers/{customerId}")]
    public async Task<IActionResult> UploadProductAsync([FromBody] UploadProductDto dto, 
        [FromRoute] int customerId ,CancellationToken ct)
    {
        var grpcRequest = new UploadProductRequest
        {
            Price = dto.Price,
            Condition = dto.Condition,
            Description = dto.Description,
            Name = dto.Name,
            Category = (Category)dto.Category,
            OtherCategory = dto.OtherCategory ?? string.Empty,
            SoldByCustomerId = customerId,
            ImageUrl = {dto.ImageUrls ?? new List<string>() } // equivalent as ImageUrl.AddRange(dto.ImageUrls)
        };

        try
        {
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

            //convert gRPC Image into ImageDto
            var images = grpcResponse.Images.Select(img => new ImageDto()
            {
                Id = img.Id,
                Url = img.Url,
                ProductId = img.ProductId
            }).ToList();

            ProductDto responseDto = new()
            {
                Id = grpcResponse.Product.Id,
                Price = grpcResponse.Product.Price,
                Sold = grpcResponse.Product.Sold,
                Condition = grpcResponse.Product.Condition,
                ApprovalStatus = grpcResponse.Product.ApprovalStatus.ToString(),
                Name = grpcResponse.Product.Name,
                Category = grpcResponse.Product.Category.ToString(),
                Description = grpcResponse.Product.Description,
                SoldByCustomerId = grpcResponse.Product.SoldByCustomerId,
                RegisterDate = grpcResponse.Product.RegisterDate.ToDateTime(),
                Images = images
            };
            return Ok(responseDto);
        }
        catch (UploadProductException ex)
        {
            return BadRequest(ex.Message);
        }
        catch (KeyNotFoundException ex)
        {
            return NotFound(ex.Message);
        }
        catch (ApplicationException ex)
        {
            return StatusCode(StatusCodes.Status500InternalServerError, ex.Message);
        }
    }
}
