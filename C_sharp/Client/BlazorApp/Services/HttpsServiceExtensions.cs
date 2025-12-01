using BlazorApp.Services.Concrete;

namespace BlazorApp.Services;

public static class HttpsServiceExtensions
{
    
    public static void AddHttpService(this IServiceCollection services)
    {
        var address = new Uri("https://localhost:6760/");
        services.AddHttpClient<HttpCustomerService>(c =>
        {
            c.BaseAddress = address;
        });
        services.AddHttpClient<HttpUploadProductService>(c =>
        {
            c.BaseAddress = address;
        });
        services.AddHttpClient<HttpUpdateCustomerService>(c =>
        {
            c.BaseAddress = address;
        });
        services.AddHttpClient<HttpProductInspectionService>(c =>
        {
            c.BaseAddress = address;
        });
        services.AddHttpClient<HttpGetProductService>(c =>
        {
            c.BaseAddress = address;
        });

    }
}
