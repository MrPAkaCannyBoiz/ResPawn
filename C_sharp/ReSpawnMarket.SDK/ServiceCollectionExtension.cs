using Com.Respawnmarket;
using Microsoft.Extensions.DependencyInjection;

namespace ReSpawnMarket.SDK;

public static class ServiceCollectionExtension
{
    private const string _grpcServerAddress = "https://localhost:6767";
    public static void AddGrpcSdk(this IServiceCollection services)
    {
        services.AddGrpcClient<CustomerRegisterService.CustomerRegisterServiceClient>(options =>
        {
            options.Address = new Uri(_grpcServerAddress); // must match the grpc server address
        });
        services.AddGrpcClient<GetCustomerService.GetCustomerServiceClient>(options =>
        {
            options.Address = new Uri(_grpcServerAddress); // must match the grpc server address
        });
        services.AddGrpcClient<UploadProductService.UploadProductServiceClient>(options =>
        {
            options.Address = new Uri(_grpcServerAddress); // must match the grpc server address
        });
    }
}
