using Com.Respawnmarket;
using Grpc.Core;
using ReSpawnMarket.SDK.ServiceExceptions;
using ReSpawnMarket.SDK.ServiceInterfaces;
using System;
using System.Collections.Generic;
using System.Text;

namespace ReSpawnMarket.SDK.Services;

public class CustomerLoginGrpcService : ICustomerLoginService
{
    private readonly CustomerLoginService.CustomerLoginServiceClient _grpcClient;
    public CustomerLoginGrpcService(CustomerLoginService.CustomerLoginServiceClient grpcClient)
    {
        _grpcClient = grpcClient;
    }
    public async Task<CustomerLoginResponse> LoginCustomerAsync(CustomerLoginRequest request
        , CancellationToken cancellationToken = default)
    {
        try
        {
            return await _grpcClient.LoginAsync(request, cancellationToken: cancellationToken);
        }
        catch (RpcException ex) when (ex.StatusCode == StatusCode.NotFound)
        {
            throw new InvalidLoginException(ex.Message); // custom exception for invalid login
        }
        catch (RpcException ex) // unexpected gRPC error
        {
            throw new ApplicationException($"gRPC {ex.StatusCode}: {ex.Status.Detail}", ex);
        }
    }
}
