using System;
using Com.Respawnmarket;
using Grpc.Core;
using ReSpawnMarket.SDK.ServiceInterfaces;

namespace ReSpawnMarket.SDK.Services;

public class PurchaseGrpcService : IPurchaseService
{
private readonly PurchaseService.PurchaseServiceClient GrpcClient;

public PurchaseGrpcService(PurchaseService.PurchaseServiceClient GrpcClient)
    {
        this.GrpcClient=GrpcClient;
    }
    public async Task<BuyProductsResponse> BuyProductsAsync(BuyProductsRequest request, CancellationToken ct = default)
    {
        try
        {
            return await  GrpcClient.BuyProductsAsync(request, cancellationToken: ct);
        }
        catch (RpcException ex) when (ex.StatusCode == StatusCode.NotFound)
        {
            throw new KeyNotFoundException("One or more products not found.", ex);
        }
        catch (RpcException ex) when (ex.StatusCode == StatusCode.FailedPrecondition)
        {
            throw new InvalidOperationException("Purchase failed.", ex);
        }
        catch (RpcException ex)
        {
            throw new ApplicationException($"gRPC {ex.StatusCode}: {ex.Status.Detail}", ex);
        }
    }
}
