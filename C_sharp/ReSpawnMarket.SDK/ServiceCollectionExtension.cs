using Com.Respawnmarket;
using Microsoft.Extensions.DependencyInjection;

namespace ReSpawnMarket.SDK;

public static class ServiceCollectionExtension
{
    private const string _grpcServerAddress = "https://localhost:6767"; // must match the grpc server address
    public static void AddGrpcSdk(this IServiceCollection services)
    {
        services.AddGrpcClient<CustomerRegisterService.CustomerRegisterServiceClient>(options =>
        {
            options.Address = new Uri(_grpcServerAddress); 
        });
        services.AddGrpcClient<GetCustomerService.GetCustomerServiceClient>(options =>
        {
            options.Address = new Uri(_grpcServerAddress); 
        });
        services.AddGrpcClient<UploadProductService.UploadProductServiceClient>(options =>
        {
            options.Address = new Uri(_grpcServerAddress);
        });
        services.AddGrpcClient<UpdateCustomerService.UpdateCustomerServiceClient>(options =>
        {
            options.Address = new Uri(_grpcServerAddress);
        });
    }
}
