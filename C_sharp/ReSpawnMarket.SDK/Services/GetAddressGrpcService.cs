using Com.Respawnmarket;
using Grpc.Core;
using ReSpawnMarket.SDK.ServiceInterfaces;
using System;
using System.Collections.Generic;
using System.Text;
using System.Threading;

namespace ReSpawnMarket.SDK.Services;

public class GetAddressGrpcService : IGetAddressService
{
   private readonly GetAddressService.GetAddressServiceClient _grpcClient;
    public GetAddressGrpcService(GetAddressService.GetAddressServiceClient grpcClient)
    {
        _grpcClient = grpcClient;
    }
    
    public async Task<GetAllPawnshopAddressesResponse> GetAllPawnshopAddressesAsync(
        GetAllPawnshopAddressesRequest request, CancellationToken ct = default)
    {
        try
        {
            var response = await _grpcClient.GetAllPawnshopAddressesAsync(request,
                cancellationToken: ct);
            return response;
        }
        catch (RpcException ex) when (ex.StatusCode == StatusCode.NotFound)
        {
            throw new KeyNotFoundException($"Addresses for Pawnshop empty {ex.Message}");
        }
        catch (RpcException ex)
        {
            throw new ApplicationException($"Unexpected gRPC error {ex.StatusCode}: {ex.Status.Detail}");
        }
    }
}
