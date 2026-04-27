using Com.Respawnmarket;
using Grpc.Net.ClientFactory;
using Microsoft.Extensions.DependencyInjection;
using ReSpawnMarket.SDK.ServiceInterfaces;

namespace ReSpawnMarket.SDK;

public static class ServiceCollectionExtension
{
    private const string _defaultGrpcServerAddress = "https://localhost:6767";

    public static void AddGrpcSdk(this IServiceCollection services)
    {
        var grpcServerAddress =
            Environment.GetEnvironmentVariable("GrpcServer__Address")
            ?? _defaultGrpcServerAddress;

        var trustSelfSigned = string.Equals(
            Environment.GetEnvironmentVariable("GrpcServer__TrustSelfSigned"),
            "true",
            StringComparison.OrdinalIgnoreCase);

        void Configure(GrpcClientFactoryOptions options)
        {
            options.Address = new Uri(grpcServerAddress);
        }

        System.Net.Http.HttpMessageHandler BuildHandler() =>
            new System.Net.Http.HttpClientHandler
            {
                ServerCertificateCustomValidationCallback = trustSelfSigned
                    ? System.Net.Http.HttpClientHandler.DangerousAcceptAnyServerCertificateValidator
                    : null
            };

        services.AddGrpcClient<CustomerRegisterService.CustomerRegisterServiceClient>(Configure)
            .ConfigurePrimaryHttpMessageHandler(BuildHandler);
        services.AddGrpcClient<GetCustomerService.GetCustomerServiceClient>(Configure)
            .ConfigurePrimaryHttpMessageHandler(BuildHandler);
        services.AddGrpcClient<UploadProductService.UploadProductServiceClient>(Configure)
            .ConfigurePrimaryHttpMessageHandler(BuildHandler);
        services.AddGrpcClient<UpdateCustomerService.UpdateCustomerServiceClient>(Configure)
            .ConfigurePrimaryHttpMessageHandler(BuildHandler);
        services.AddGrpcClient<ProductInspectionService.ProductInspectionServiceClient>(Configure)
            .ConfigurePrimaryHttpMessageHandler(BuildHandler);
        services.AddGrpcClient<GetProductService.GetProductServiceClient>(Configure)
            .ConfigurePrimaryHttpMessageHandler(BuildHandler);
        services.AddGrpcClient<CustomerLoginService.CustomerLoginServiceClient>(Configure)
            .ConfigurePrimaryHttpMessageHandler(BuildHandler);
        services.AddGrpcClient<ResellerLoginService.ResellerLoginServiceClient>(Configure)
            .ConfigurePrimaryHttpMessageHandler(BuildHandler);
        services.AddGrpcClient<PurchaseService.PurchaseServiceClient>(Configure)
            .ConfigurePrimaryHttpMessageHandler(BuildHandler);
        services.AddGrpcClient<CustomerInspectionService.CustomerInspectionServiceClient>(Configure)
            .ConfigurePrimaryHttpMessageHandler(BuildHandler);
        services.AddGrpcClient<GetAddressService.GetAddressServiceClient>(Configure)
            .ConfigurePrimaryHttpMessageHandler(BuildHandler);
    }
}
