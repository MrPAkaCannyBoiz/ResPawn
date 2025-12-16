using Com.Respawnmarket;
using System;
using System.Collections.Generic;
using System.Text;

namespace ReSpawnMarket.SDK.ServiceInterfaces;

public interface IGetAddressService
{
    Task<GetAllPawnshopAddressesResponse> GetAllPawnshopAddressesAsync(GetAllPawnshopAddressesRequest request
          , CancellationToken ct = default);
}
