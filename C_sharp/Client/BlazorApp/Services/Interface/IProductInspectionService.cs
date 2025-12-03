using System;
using ApiContracts.Dtos;

namespace BlazorApp.Services.Interface;

public interface IProductInspectionService
{
    public Task<IReadOnlyList<ProductDto>> GetPendingProductsAsync();
    public Task<DetailedProductDto> GetProductDetailsAsync(int productId);

    public Task<ProductInspectionResultDto> ReviewProductAsync(int productId, ProductInspectionDto dto);
}