using Com.Respawnmarket;
using System;
using System.Collections.Generic;
using System.Text;

namespace ReSpawnMarket.SDK.ServiceInterfaces;

public interface ICustomerInspectionService
{
    public Task<EnableSellingResponse> SetCanSellAsync(EnableSellingRequest request, 
        CancellationToken ct = default);
}
