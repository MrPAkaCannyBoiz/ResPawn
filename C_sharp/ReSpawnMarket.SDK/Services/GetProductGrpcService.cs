using Com.Respawnmarket;
using Grpc.Core;
using ReSpawnMarket.SDK.ServiceInterfaces;
using System;
using System.Collections.Generic;
using System.Text;

namespace ReSpawnMarket.SDK.Services;

// TODO: handle exceptions for gRPC calls (not found and failed precondition) for single product fetch
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
        catch (RpcException ex) when (ex.StatusCode == StatusCode.FailedPrecondition)
        {
            throw new KeyNotFoundException($"Relations incompleted : {ex.Message}", ex);
        }
        catch (RpcException ex) when (ex.StatusCode == StatusCode.NotFound)
        {
            throw new KeyNotFoundException($"All product not found : {ex.Message}", ex);
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
        catch (RpcException ex) when (ex.StatusCode == StatusCode.FailedPrecondition)
        {
            throw new KeyNotFoundException($"Relations incompleted : {ex.Message}", ex);
        }
        catch (RpcException ex) when (ex.StatusCode == StatusCode.NotFound)
        {
            throw new KeyNotFoundException($"Pending product not found : {ex.Message}", ex);
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
        catch (RpcException ex) when (ex.StatusCode == StatusCode.NotFound)
        {
            throw new KeyNotFoundException($"Product with ID {request.ProductId} not found.", ex);
        }
        catch (RpcException ex) when (ex.StatusCode == StatusCode.FailedPrecondition)
        {
            throw new KeyNotFoundException($"Relations incompleted : {ex.Message}", ex);
        }
        catch (RpcException ex)
        {
            throw new Exception($"gRPC Error: {ex.Status.Detail}", ex);
        }
    }

    public async Task<GetAllAvailableProductsResponse> GetAllAvailableProductsAsync(GetAllAvailableProductsRequest request,
        CancellationToken ct = default)
    {
        try
        {
            return await _grpcClient.GetAllAvailableProductsAsync(request, cancellationToken: ct);
        }
        catch (RpcException ex) when (ex.StatusCode == StatusCode.NotFound)
        {
            throw new KeyNotFoundException($"All product sold out : {ex.Message}", ex);
        }
        catch (RpcException ex) when (ex.StatusCode == StatusCode.FailedPrecondition)
        {
            throw new KeyNotFoundException($"Relations incompleted : {ex.Message}", ex);
        }
        catch (RpcException ex) 
        {

            throw new Exception($"gRPC Error: {ex.Status.Detail}", ex);
        }
    }
}
