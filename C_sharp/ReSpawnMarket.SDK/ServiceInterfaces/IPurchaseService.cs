using System;
using Com.Respawnmarket;

namespace ReSpawnMarket.SDK.ServiceInterfaces;

public interface IPurchaseService
{
    Task<BuyProductsResponse> BuyProductsAsync(BuyProductsRequest request, CancellationToken ct = default);

}
