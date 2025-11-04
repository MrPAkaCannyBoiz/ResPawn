using Com.Respawnmarket;
using Grpc.Core;
using ReSpawnMarket.SDK.ServiceInterfaces;
using System;
using System.Collections.Generic;
using System.Text;

namespace ReSpawnMarket.SDK.Services;

public class GetCustomerGrpcService : IGetCustomerService
{
    private readonly GetCustomerService.GetCustomerServiceClient _grpcClient;

    public GetCustomerGrpcService(GetCustomerService.GetCustomerServiceClient grpcClient)
    {
        _grpcClient = grpcClient;
    }
    public async Task<GetCustomerResponse> GetCustomerAsync(GetCustomerRequest request
        , CancellationToken cancellationToken = default)
    {
        try
        {
            var response = await _grpcClient.GetCustomerAsync(request,
                cancellationToken: cancellationToken);
            return response;
        }
        catch (RpcException ex)
        {
            throw new ApplicationException($"gRPC {ex.StatusCode}: {ex.Status.Detail}", ex);
        }
    }
}
