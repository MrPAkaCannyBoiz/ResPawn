using Com.Respawnmarket;
using Grpc.Core;
using ReSpawnMarket.SDK.ServiceInterfaces;
using System;
using System.Collections.Generic;
using System.Text;

namespace ReSpawnMarket.SDK.Services;

public class GetProductGrpcService : IGetProductService
{
    private readonly GetProductService.GetProductServiceClient _grpcClient;
    public GetProductGrpcService(GetProductService.GetProductServiceClient grpcClient)
    {
        _grpcClient = grpcClient;
    }
    public Task<GetAllProductsResponse> GetAllProductsAsync(
        GetAllProductsRequest request, CancellationToken ct = default)
    {
        try
        {
            return _grpcClient.GetAllProductsAsync(request, cancellationToken: ct).ResponseAsync;
        }
        catch (RpcException ex)
        {
            throw new Exception($"gRPC Error: {ex.Status.Detail}", ex);
        }
    }

    public Task<GetPendingProductsResponse> GetPendingProductsAsync(
        GetPendingProductsRequest request, CancellationToken ct = default)
    {
        try
        {
            return _grpcClient.GetPendingProductsAsync(request, cancellationToken: ct).ResponseAsync;
        }
        catch (RpcException ex)
        {
            throw new Exception($"gRPC Error: {ex.Status.Detail}", ex);
        }
    }

    public async Task<GetProductResponse> GetProductAsync(
        GetProductRequest request, CancellationToken ct = default)
    {
        try
        {
            return await _grpcClient.GetProductAsync(request, cancellationToken: ct);
        }
        catch (RpcException ex)
        {
            throw new Exception($"gRPC Error: {ex.Status.Detail}", ex);
        }
    }
}
