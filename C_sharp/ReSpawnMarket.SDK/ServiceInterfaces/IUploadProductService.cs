using Com.Respawnmarket;
using System;
using System.Collections.Generic;
using System.Text;

namespace ReSpawnMarket.SDK.ServiceInterfaces;

public interface IUploadProductService
{
    Task<UploadProductResponse> UploadProductAsync(UploadProductRequest request
        , CancellationToken cancellationToken = default);
}
