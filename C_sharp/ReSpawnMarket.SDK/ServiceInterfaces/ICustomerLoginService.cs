using Com.Respawnmarket;
using System;
using System.Collections.Generic;
using System.Text;

namespace ReSpawnMarket.SDK.ServiceInterfaces;

public interface ICustomerLoginService
{
    Task<CustomerLoginResponse> LoginCustomerAsync(CustomerLoginRequest request
          , CancellationToken cancellationToken = default);
}
