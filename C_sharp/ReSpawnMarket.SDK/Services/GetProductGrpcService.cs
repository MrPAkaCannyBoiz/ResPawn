using Com.Respawnmarket;
using Grpc.Core;
using ReSpawnMarket.SDK.ServiceExceptions;
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
            throw new ProductNotFoundException($"All product not found : {ex.Message}");
        }
        catch (RpcException ex)
        {
            throw new ApplicationException($"gRPC Error: {ex.Status.Detail}", ex);
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
            throw new ProductNotFoundException($"Pending product not found : {ex.Message}");
        }
        catch (RpcException ex)
        {
            throw new ApplicationException($"gRPC Error: {ex.Status.Detail}", ex);
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
            throw new ProductNotFoundException($"Product with ID {request.ProductId} not found.");
        }
        catch (RpcException ex) when (ex.StatusCode == StatusCode.FailedPrecondition)
        {
            throw new KeyNotFoundException($"Relations incompleted : {ex.Message}", ex);
        }
        catch (RpcException ex)
        {
            throw new ApplicationException($"gRPC Error: {ex.Status.Detail}", ex);
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
            throw new ProductNotFoundException($"All product sold out : {ex.Message}");
        }
        catch (RpcException ex) when (ex.StatusCode == StatusCode.FailedPrecondition)
        {
            throw new KeyNotFoundException($"Relations incompleted : {ex.Message}", ex);
        }
        catch (RpcException ex) 
        {
            throw new ApplicationException($"gRPC Error: {ex.Status.Detail}", ex);
        }
    }

    public async Task<GetAllReviewingProductsResponse> GetAllReviewingProductsAsync(GetAllReviewingProductsRequest request,
        CancellationToken ct = default)
    {
        try
        {
            return await _grpcClient.GetAllReviewingProductsAsync(request, cancellationToken: ct);
        }
        catch (RpcException ex) when (ex.StatusCode == StatusCode.NotFound)
        {
            throw new ProductNotFoundException($"All reviewing products not found : {ex.Message}");
        }
        catch (RpcException ex) when (ex.StatusCode == StatusCode.FailedPrecondition)
        {
            throw new KeyNotFoundException($"Relations incompleted : {ex.Message}", ex);
        }
        catch (RpcException ex)
        {
            throw new ApplicationException($"gRPC Error: {ex.Status.Detail}", ex);
        }
    }
}
