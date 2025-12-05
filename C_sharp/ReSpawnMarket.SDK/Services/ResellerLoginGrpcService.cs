using System;
using Com.Respawnmarket;
using Grpc.Core;
using ReSpawnMarket.SDK.ServiceExceptions;
using ReSpawnMarket.SDK.ServiceInterfaces;

namespace ReSpawnMarket.SDK.Services;

public class ResellerLoginGrpcService : IResellerLoginService
{
    private readonly ResellerLoginService.ResellerLoginServiceClient _grpcClient;

    public ResellerLoginGrpcService(ResellerLoginService.ResellerLoginServiceClient _grpcClient)
    {
        this._grpcClient =_grpcClient;
    }
    public async Task<ResellerLoginResponse> LoginResellerAsync(ResellerLoginRequest request, CancellationToken cancellationToken = default)
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

