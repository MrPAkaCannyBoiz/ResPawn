using Com.Respawnmarket;
using Microsoft.Extensions.DependencyInjection;

namespace ReSpawnMarket.SDK;

public static class ServiceCollectionExtension
{
    public static void AddGrpcSdk(this IServiceCollection services)
    {
        services.AddGrpcClient<CustomerRegisterService.CustomerRegisterServiceClient>(options =>
        {
            options.Address = new Uri("http://localhost:6767"); // must match the grpc server address
        });
    }
}
