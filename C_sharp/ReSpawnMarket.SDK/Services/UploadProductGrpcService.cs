using Com.Respawnmarket;
using Grpc.Core;
using ReSpawnMarket.SDK.ServiceInterfaces;
using System;
using System.Collections.Generic;
using System.Text;

namespace ReSpawnMarket.SDK.Services;

public class UploadProductGrpcService : IUploadProductService
{
    private readonly UploadProductService.UploadProductServiceClient _grpcClient;

    public UploadProductGrpcService(UploadProductService.UploadProductServiceClient grpcClient)
    {
        _grpcClient = grpcClient;
    }
    public async Task<UploadProductResponse> UploadProductAsync(UploadProductRequest request
        , CancellationToken cancellationToken = default)
    {
        try
        {
            var response = await _grpcClient
                .UploadProductAsync(request, cancellationToken: cancellationToken);
            return response;
        }
        catch (RpcException ex)
        {
            throw new ApplicationException($"gRPC Error: {ex.StatusCode}: {ex.Status.Detail}", ex);
        }
    }
}
