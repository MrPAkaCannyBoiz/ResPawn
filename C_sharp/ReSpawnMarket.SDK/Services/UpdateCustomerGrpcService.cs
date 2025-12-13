using Com.Respawnmarket;
using Grpc.Core;
using ReSpawnMarket.SDK.ServiceExceptions;
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
    public async Task<UpdateCustomerResponse> UpdateCustomerAsync(UpdateCustomerRequest request
        , CancellationToken cancellationToken = default)
    {
        try
        {
            return await _grpcClient.UpdateCustomerAsync(request, cancellationToken: cancellationToken);
        }
        catch (RpcException ex) when (ex.StatusCode == StatusCode.InvalidArgument
                                       || ex.StatusCode == StatusCode.AlreadyExists)
        {
            throw new UpdateCustomerException($"Error while updating the customer {ex.Message}");
        }
        catch (RpcException ex) when (ex.StatusCode == StatusCode.NotFound)
        {
            throw new KeyNotFoundException(
                $"The customer to update was not found. {ex.Message}");
        }
        catch (RpcException ex) when (ex.StatusCode == StatusCode.Internal)
        {
            throw new ApplicationException(
                $"An internal server error occurred while updating the customer. {ex.Message}");
        }
        catch (RpcException ex)
        {
            throw new ApplicationException($"An error occurred while updating the customer. " +
                $"{ex.StatusCode}: {ex.Status.Detail})", ex);
        }
    }
}
