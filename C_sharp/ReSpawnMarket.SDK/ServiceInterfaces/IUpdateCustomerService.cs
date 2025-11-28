using Com.Respawnmarket;
using System;
using System.Collections.Generic;
using System.Text;

namespace ReSpawnMarket.SDK.ServiceInterfaces;

public interface IUpdateCustomerService
{
    Task<UpdateCustomerResponse> UpdateCustomerAsync(UpdateCustomerRequest request
          , CancellationToken cancellationToken = default);
}
