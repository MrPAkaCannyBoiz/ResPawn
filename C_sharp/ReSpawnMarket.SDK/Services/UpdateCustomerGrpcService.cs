using Com.Respawnmarket;
using Grpc.Core;
using ReSpawnMarket.SDK.ServiceInterfaces;
using System;
using System.Collections.Generic;
using System.Text;

namespace ReSpawnMarket.SDK.Services;

public class UpdateCustomerGrpcService : IUpdateCustomerService
{
    private readonly UpdateCustomerService.UpdateCustomerServiceClient _grpcClient;

    public UpdateCustomerGrpcService(UpdateCustomerService.UpdateCustomerServiceClient grpcClient)
    {
        _grpcClient = grpcClient;
    }
    public Task<UpdateCustomerResponse> UpdateCustomerAsync(UpdateCustomerRequest request
        , CancellationToken cancellationToken = default)
    {
        try
        {
            return _grpcClient.UpdateCustomerAsync(request, cancellationToken: cancellationToken).ResponseAsync;
        }
        catch (RpcException ex)
        {
            throw new ApplicationException($"An error occurred while updating the customer. " +
                $"{ex.StatusCode}: {ex.Status.Detail})", ex);
        }
    }
}
