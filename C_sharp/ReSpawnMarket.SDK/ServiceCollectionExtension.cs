using Com.Respawnmarket;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Options;
using ReSpawnMarket.SDK.ServiceInterfaces;

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
        services.AddGrpcClient<ProductInspectionService.ProductInspectionServiceClient>(options =>
        {
            options.Address = new Uri(_grpcServerAddress);
        });
        services.AddGrpcClient<GetProductService.GetProductServiceClient>(options =>
        {
            options.Address = new Uri(_grpcServerAddress);
        });
        services.AddGrpcClient<CustomerLoginService.CustomerLoginServiceClient>(options =>
        {
            options.Address = new Uri(_grpcServerAddress);
        });
        services.AddGrpcClient<ResellerLoginService.ResellerLoginServiceClient>(options =>
        {
            options.Address = new Uri(_grpcServerAddress);
        });
        services.AddGrpcClient<UpdateCustomerService.UpdateCustomerServiceClient>(options =>
        {
            options.Address = new Uri(_grpcServerAddress);
        });
        services.AddGrpcClient<ProductInspectionService.ProductInspectionServiceClient>(options =>
        {
            options.Address = new Uri(_grpcServerAddress);
        });
        services.AddGrpcClient<GetProductService.GetProductServiceClient>(options =>
        {
            options.Address = new Uri(_grpcServerAddress);
        });
        services.AddGrpcClient<PurchaseService.PurchaseServiceClient>(options =>
        {
            options.Address = new Uri(_grpcServerAddress);
        });
        services.AddGrpcClient<CustomerInspectionService.CustomerInspectionServiceClient>(options =>
        {
            options.Address = new Uri(_grpcServerAddress);
        });
    }
}
