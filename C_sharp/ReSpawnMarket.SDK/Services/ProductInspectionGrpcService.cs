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

    public async Task<ProductInspectionResponse> ReviewProductAsync(ProductInspectionRequest request,
        CancellationToken cancellationToken = default)
    {
        try
        {
            return await _grpcClient.ReviewProductAsync(request, cancellationToken: cancellationToken);
        }
        catch (RpcException ex) when (ex.StatusCode == StatusCode.NotFound)
        {
            throw new KeyNotFoundException($"Product with ID {request.ProductId} not found.");
        }
        catch (RpcException ex) when (ex.StatusCode == StatusCode.InvalidArgument)
        {
            throw new ArgumentException($"Invalid argument: {ex.Status.Detail}");
        }
        catch (RpcException ex)
        {
            throw new Exception($"gRPC Error: {ex.Status.Detail}", ex);
        }
    }

    public async Task<ProductVerificationResponse> VerifyProductAsync(ProductVerificationRequest request,
        CancellationToken cancellationToken = default)
    {
        try
        {
            return await _grpcClient.VerifyProductAsync(request, cancellationToken: cancellationToken);
        }
        catch (RpcException ex) when (ex.StatusCode == StatusCode.NotFound)
        {
        throw new KeyNotFoundException($"Product/Reseller/Pawnshop not found {ex.Status.Detail}");          }
        catch (RpcException ex) when (ex.StatusCode == StatusCode.InvalidArgument)
        {
            throw new ArgumentException($"Invalid argument: {ex.Status.Detail}");
        }
        catch (RpcException ex)
        {
            throw new Exception($"gRPC Error: {ex.Status.Detail}", ex);
        }
    }
}
