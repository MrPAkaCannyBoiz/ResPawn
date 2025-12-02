using Com.Respawnmarket;
using System;
using System.Collections.Generic;
using System.Text;

namespace ReSpawnMarket.SDK.ServiceInterfaces;

public interface IGetProductService
{
    Task<GetProductResponse> GetProductAsync(GetProductRequest request
          , CancellationToken ct = default);
    Task<GetAllProductsResponse> GetAllProductsAsync(GetAllProductsRequest request
          , CancellationToken ct = default);
    Task<GetPendingProductsResponse> GetPendingProductsAsync(GetPendingProductsRequest request
          , CancellationToken ct = default);
    Task<GetAllAvailableProductsResponse> GetAllAvailableProductsAsync(GetAllAvailableProductsRequest request
          , CancellationToken ct = default);
}
