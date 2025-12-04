using Com.Respawnmarket;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace ReSpawnMarket.SDK.ServiceInterfaces
{
    public interface IProductInspectionService
    {
        Task<ProductInspectionResponse> ReviewProductAsync(
            ProductInspectionRequest request,
            CancellationToken cancellationToken = default);

        Task<ProductVerificationResponse> VerifyProductAsync(
            ProductVerificationRequest request,
            CancellationToken cancellationToken = default);

    }
}