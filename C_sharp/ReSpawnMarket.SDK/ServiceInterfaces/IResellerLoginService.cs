using System;
using Com.Respawnmarket;

namespace ReSpawnMarket.SDK.ServiceInterfaces;

public interface IResellerLoginService
{
 
   Task<ResellerLoginResponse> LoginResellerAsync(ResellerLoginRequest request
   , CancellationToken cancellationToken=default);

}
