using Com.Respawnmarket;
using System;
using System.Collections.Generic;
using System.Text;

namespace ReSpawnMarket.SDK.ServiceInterfaces;

public interface IRegisterCustomerService
{
    Task<RegisterCustomerResponse> RegisterCustomerAsync(RegisterCustomerRequest request
        , CancellationToken cancellationToken = default);
}
