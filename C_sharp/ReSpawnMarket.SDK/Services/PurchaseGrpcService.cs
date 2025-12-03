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
        catch(RpcException ex)
        {
            throw new ApplicationException($"gRPC {ex.StatusCode}: {ex.Status.Detail}", ex);
        }
    }
}
