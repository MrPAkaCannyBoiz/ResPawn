using System;
using System.Collections.Generic;
using System.Linq;
using System.Runtime.CompilerServices;
using System.Threading.Tasks;
using ApiContracts.Dtos;
using Com.Respawnmarket;
using Microsoft.AspNetCore.Mvc;
using ReSpawnMarket.SDK.ServiceInterfaces;

namespace WebAPI.Controllers;

[ApiController]
[Route("inspection")]
public class ProductInspectionController : ControllerBase
{
    private readonly IProductInspectionService service;

    public ProductInspectionController(IProductInspectionService service)
    {
        this.service = service;
    }

    [HttpPost("product/{productId}")]
    [ProducesResponseType(typeof(UploadProductDto), StatusCodes.Status200OK)]
    [ProducesResponseType(typeof(ProblemDetails), StatusCodes.Status400BadRequest)]
    [ProducesResponseType(typeof(ProblemDetails), StatusCodes.Status502BadGateway)]
    public async Task<IActionResult> ReviewProductAsync(int productId, 
        ProductInspectionDto dto ,CancellationToken ct)
    {
        var request = new ProductInspectionRequest
        {
            ProductId = productId,
            ResellerId = dto.ResellerId,
            Comments = dto.Comments,
            IsAccepted = dto.IsAccepted,
            PawnshopId = dto.PawnshopId
        };
        try
        {
            var response = await service.ReviewProductAsync(request, ct);
            var resultDto = new ProductInspectionResultDto
            {
                ProductId = response.ProductId,
                ApprovalStatus = ToDtoApprovalStatus(response.ApprovalStatus),
                PawnshopId = response.PawnshopId
            };
            return Ok(resultDto);
        }
        catch (NotImplementedException ex)
        {
            return StatusCode(StatusCodes.Status502BadGateway, new ProblemDetails
            {
                Title = "Service Not Implemented",
                Detail = ex.Message
            });
        }
    }

    [HttpPost("product/verify/{productId}")]
    [ProducesResponseType(typeof(UploadProductDto), StatusCodes.Status200OK)]
    [ProducesResponseType(typeof(ProblemDetails), StatusCodes.Status400BadRequest)]
    [ProducesResponseType(typeof(ProblemDetails), StatusCodes.Status502BadGateway)]
    public async Task<IActionResult> VerifyProductAsync (int productId,
        ProductVerificationDto dto, CancellationToken ct)
    {
        var request = new ProductVerificationRequest
        {
            ProductId = productId,
            ResellerId = dto.ResellerId,
            Comments = dto.Comments,
            IsAccepted = dto.IsAccepted
        };
        try
        {
            var response = await service.VerifyProductAsync(request, ct);
            
            var resultDto = new ProductInspectionResultDto
            {
                ProductId = response.ProductId,
                ApprovalStatus = ToDtoApprovalStatus(response.ApprovalStatus)
            };
            return Ok(resultDto);
        }
        catch (NotImplementedException ex)
        {
            return StatusCode(StatusCodes.Status502BadGateway, new ProblemDetails
            {
                Title = "Service Not Implemented",
                Detail = ex.Message
            });
        }
    }

    private ApiContracts.Dtos.Enums.ApprovalStatus ToDtoApprovalStatus(ApprovalStatus status)
    {
        return status switch
        {
            ApprovalStatus.Approved => ApiContracts.Dtos.Enums.ApprovalStatus.APPROVED,
            ApprovalStatus.Reviewing => ApiContracts.Dtos.Enums.ApprovalStatus.REVIEWING,
            ApprovalStatus.Rejected => ApiContracts.Dtos.Enums.ApprovalStatus.REJECTED,
            ApprovalStatus.Pending => ApiContracts.Dtos.Enums.ApprovalStatus.PENDING,
            _ => throw new NotImplementedException(),
        };
    }

}
