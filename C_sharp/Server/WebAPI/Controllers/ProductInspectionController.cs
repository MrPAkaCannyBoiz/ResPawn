using System;
using System.Collections.Generic;
using System.Linq;
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
            ApiContracts.Dtos.Enums.ApprovalStatus approvalStatus = response.ApprovalStatus switch
            {
                ApprovalStatus.Approved => ApiContracts.Dtos.Enums.ApprovalStatus.APPROVED,
                ApprovalStatus.NotApproved => ApiContracts.Dtos.Enums.ApprovalStatus.NOT_APPROVED,
                ApprovalStatus.Rejected => ApiContracts.Dtos.Enums.ApprovalStatus.REJECTED,
                ApprovalStatus.Pending => ApiContracts.Dtos.Enums.ApprovalStatus.PENDING,
                _ => throw new NotImplementedException(),
            };
            var resultDto = new ProductInspectionResultDto
            {
                ProductId = response.ProductId,
                ApprovalStatus = approvalStatus,
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


}
