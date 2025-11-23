using Com.Respawnmarket;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using ApiContracts.Dtos.ProductDto;

namespace ReSpawnMarket.SDK.ServiceInterfaces
{
    public interface IProductInspectionService
    {
        // two methods, get products (moved later because violate SRP) and review product
        Task<GetPendingProductsResponse> GetPendingProductsAsync(GetPendingProductsRequest request,
            CancellationToken cancellationToken = default);
        Task<ProductInspectionResponse> ReviewProductAsync(
            ProductInspectionRequest request,
            CancellationToken cancellationToken = default);

    }
}