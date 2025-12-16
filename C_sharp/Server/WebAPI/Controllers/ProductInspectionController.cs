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
[Route("/api/inspection")]
public class ProductInspectionController : ControllerBase
{
    private readonly IProductInspectionService service;

    public ProductInspectionController(IProductInspectionService service)
    {
        this.service = service;
    }

    [HttpPost("product/{productId}")]
    [ProducesResponseType(typeof(ProductInspectionResultDto), StatusCodes.Status200OK)]
    public async Task<IActionResult> ReviewProductAsync(int productId, 
        ProductInspectionDto dto ,CancellationToken ct)
    {
        try
        {
            var request = new ProductInspectionRequest
            {
                ProductId = productId,
                ResellerId = dto.ResellerId,
                Comments = dto.Comments,
                IsAccepted = dto.IsAccepted,
                PawnshopId = dto.PawnshopId
            };
            var response = await service.ReviewProductAsync(request, ct);
            var resultDto = new ProductInspectionResultDto
            {
                ProductId = response.ProductId,
                ApprovalStatus = ToDtoApprovalStatus(response.ApprovalStatus),
                PawnshopId = response.PawnshopId,
                Comments = response.Comments
            };
            return Ok(resultDto);
        }
        catch (KeyNotFoundException ex)
        {
            return NotFound(ex.Message);
        }
        catch (ArgumentException ex)
        {
            return BadRequest(ex.Message);
        }
        catch (ApplicationException ex)
        {
            return StatusCode(StatusCodes.Status500InternalServerError, ex.Message);
        }
    }

    [HttpPost("product/verify/{productId}")]
    [ProducesResponseType(typeof(ProductVerificationResultDto), StatusCodes.Status200OK)]
    public async Task<IActionResult> VerifyProductAsync (int productId,
        ProductVerificationDto dto, CancellationToken ct)
    {
        try
        {
            var request = new ProductVerificationRequest
            {
                ProductId = productId,
                ResellerId = dto.ResellerId,
                Comments = dto.Comments,
                IsAccepted = dto.IsAccepted
            };
            var response = await service.VerifyProductAsync(request, ct);

            var resultDto = new ProductVerificationResultDto
            {
                ProductId = response.ProductId,
                ApprovalStatus = ToDtoApprovalStatus(response.ApprovalStatus),
                Comments = response.Comments
            };
            return Ok(resultDto);
        }
        catch (KeyNotFoundException ex)
        {
            return NotFound(ex.Message);
        }
        catch (ArgumentException ex)
        {
            return BadRequest(ex.Message);
        }
        catch (ApplicationException ex)
        {
            return StatusCode(StatusCodes.Status500InternalServerError, ex.Message);
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
