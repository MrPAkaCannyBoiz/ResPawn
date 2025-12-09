using System;
using ApiContracts.Dtos;

namespace BlazorApp.Services.Interface;

public interface IProductInspectionClient
{
   Task<ProductInspectionResultDto?> GetLatestInspectionAsync(
            int productId,
            CancellationToken ct = default);
            Task<ProductInspectionResultDto?> SafeGetLatestInspectionAsync(int productId);
}
