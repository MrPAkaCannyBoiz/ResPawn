using ApiContracts.Dtos;
using BlazorApp.Services.Interface;
using ReSpawnMarket.SDK.ServiceInterfaces;
using System.Text.Json;

namespace BlazorApp.Services.Concrete;

public class HttpUploadProductService : Interface.IUploadProductService
{
    private readonly HttpClient _client;

    public HttpUploadProductService(HttpClient client)
    {
        _client = client;
    }

    public async Task<ProductDto> UploadProductAsync(int customerId, UploadProductDto request)
    {
       var apiRequest = await _client.PostAsJsonAsync($"api/products/customers/{customerId}", request);
       string apiStringResponse = await apiRequest.Content.ReadAsStringAsync();
       if (!apiRequest.IsSuccessStatusCode)
       {
           throw new Exception($"Error uploading product: {apiStringResponse}");
       }
       ProductDto productDto = JsonSerializer
            .Deserialize<ProductDto>(apiStringResponse, 
            JsonCaseInsensitiveExtension.MakeJsonCaseInsensitive())!;
       return productDto;
    }




}
