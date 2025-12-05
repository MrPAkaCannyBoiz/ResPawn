using Com.Respawnmarket;
using System;
using System.Collections.Generic;
using System.Text;

namespace ReSpawnMarket.SDK.ServiceInterfaces;

public interface IGetCustomerService
{
    Task<GetCustomerResponse> GetCustomerAsync(GetCustomerRequest request
          , CancellationToken cancellationToken = default);
    Task<GetAllCustomersResponse> GetAllCustomerAsync(GetAllCustomersRequest request
          , CancellationToken cancellationToken = default);
}
