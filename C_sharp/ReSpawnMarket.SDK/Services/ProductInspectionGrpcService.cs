using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

using Com.Respawnmarket;
using Grpc.Core;
using ReSpawnMarket.SDK.ServiceInterfaces;

namespace ReSpawnMarket.SDK.Services;

public class ProductInspectionGrpcService : IProductInspectionService
{
    private readonly ProductInspectionService.ProductInspectionServiceClient _grpcClient;

    public ProductInspectionGrpcService(ProductInspectionService.ProductInspectionServiceClient grpcClient)
    {
        _grpcClient = grpcClient;
    }

    public async Task<GetPendingProductsResponse> GetPendingProductsAsync
        (GetPendingProductsRequest request, CancellationToken cancellationToken = default)
    {
        try
        {
            GetPendingProductsResponse response =  await _grpcClient
                .GetPendingProductsAsync(request, cancellationToken: cancellationToken);
            return response;
        }
        catch (RpcException ex)
        {
            // Handle gRPC-specific exceptions
            throw new Exception($"gRPC Error: {ex.Status.Detail}", ex);
        }
    }


    public Task<ProductInspectionResponse> ReviewProductAsync(ProductInspectionRequest request, CancellationToken cancellationToken = default)
    {
        try
        {
            return _grpcClient.ReviewProductAsync(request, cancellationToken: cancellationToken).ResponseAsync;
        }
        catch (RpcException ex)
        {
            // Handle gRPC-specific exceptions
            throw new Exception($"gRPC Error: {ex.Status.Detail}", ex);
        }
    }
}
