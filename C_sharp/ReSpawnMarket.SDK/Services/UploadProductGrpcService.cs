using Com.Respawnmarket;
using Grpc.Core;
using ReSpawnMarket.SDK.ServiceExceptions;
using ReSpawnMarket.SDK.ServiceInterfaces;
using System;
using System.Collections.Generic;
using System.Text;

namespace ReSpawnMarket.SDK.Services;

public class UploadProductGrpcService : IUploadProductService
{
    private readonly UploadProductService.UploadProductServiceClient _grpcClient;

    public UploadProductGrpcService(UploadProductService.UploadProductServiceClient grpcClient)
    {
        _grpcClient = grpcClient;
    }
    public async Task<UploadProductResponse> UploadProductAsync(UploadProductRequest request
        , CancellationToken cancellationToken = default)
    {
        try
        {
            var response = await _grpcClient
                .UploadProductAsync(request, cancellationToken: cancellationToken);
            return response;
        }
        catch (RpcException ex) when (ex.StatusCode == StatusCode.InvalidArgument 
                                        || ex.StatusCode == StatusCode.OutOfRange) // known errors from gRPC server
        {
            throw new UploadProductException($"Upload Product failed: {ex.Status.Detail}");
        }
        catch (RpcException ex) when (ex.StatusCode == StatusCode.NotFound) // known errors from gRPC server
        {
            throw new KeyNotFoundException($"Customer to upload not found: {ex.Status.Detail}");
        }
        catch (RpcException ex) // unexpected gRPC error
        {
            throw new ApplicationException($"unknown gRPC Error: {ex.StatusCode}: {ex.Status.Detail}", ex);
        }
    }
}
