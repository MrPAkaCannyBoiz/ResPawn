using Com.Respawnmarket;
using Grpc.Core;
using ReSpawnMarket.SDK.ServiceInterfaces;
using System;
using System.Collections.Generic;
using System.Text;

namespace ReSpawnMarket.SDK.Services;

public class CustomerInspectionGrpcService : ICustomerInspectionService
{
    public readonly CustomerInspectionService.CustomerInspectionServiceClient _grpcClient;
    public CustomerInspectionGrpcService(CustomerInspectionService.CustomerInspectionServiceClient grpcClient)
    {
        _grpcClient = grpcClient;
    }
    public async Task<EnableSellingResponse> SetCanSellAsync(EnableSellingRequest request,
        CancellationToken ct = default)
    {
        try
        {
            return await _grpcClient.SetCanSellAsync(request, cancellationToken: ct);
        }
        catch (RpcException ex) when (ex.StatusCode == StatusCode.NotFound)
        {
            throw new KeyNotFoundException("The customer was not found.", ex);
        }
        catch (RpcException ex)
        {
            throw new ApplicationException("An error occurred while setting the selling status.", ex);
        }
    }
}
