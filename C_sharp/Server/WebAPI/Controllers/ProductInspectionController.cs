using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Mvc;
using ReSpawnMarket.SDK.ServiceInterfaces;
using ReSpawnMarket.Shared;

namespace WebAPI.Controllers;

[ApiController]
[Route("api/product-inspection")]
public class ProductInspectionController : ControllerBase
{
    private readonly IProductInspectionService service;

    public ProductInspectionController(IProductInspectionService service)
    {
        this.service = service;
    }

    [HttpGet("pending")]
    public async Task<IActionResult> GetPending()
    {
        var products = await service.GetPendingProductsAsync();
        return Ok(products);
    }

    [HttpPost("review")]
    public async Task<IActionResult> Review(ProductInspectionRequestDto dto)
    {
        var result = await service.ReviewProductAsync(dto);
        return Ok(result);
    }
}
