using ApiContracts.Dtos;
using ReSpawnMarket.SDK.ServiceInterfaces;
using System.Text.Json;

namespace BlazorApp.Services;

public class HttpUploadProductService : IUploadProductService
{
    private readonly HttpClient _client;

    public HttpUploadProductService(HttpClient client)
    {
        this._client = client;
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
