using Com.Respawnmarket;
using Grpc.Core;
using ReSpawnMarket.SDK.ServiceInterfaces;
using System;
using System.Collections.Generic;
using System.Text;

namespace ReSpawnMarket.SDK.Services;

public class CustomerRegisterGrpcService : IRegisterCustomerService
{
    private readonly CustomerRegisterService.CustomerRegisterServiceClient _grpcClient;

    public CustomerRegisterGrpcService(CustomerRegisterService.CustomerRegisterServiceClient grpcClient)
    {
        _grpcClient = grpcClient;
    }

    public async Task<RegisterCustomerResponse> RegisterCustomerAsync(RegisterCustomerRequest request
        , CancellationToken cancellationToken = default)
    {
        try
        {
            var response = await _grpcClient.RegisterCustomerAsync(request, 
                cancellationToken: cancellationToken);
            return response;
        }
        catch (RpcException ex)
        {
            throw new ApplicationException($"gRPC {ex.StatusCode}: {ex.Status.Detail}", ex);
        }
    }

}
