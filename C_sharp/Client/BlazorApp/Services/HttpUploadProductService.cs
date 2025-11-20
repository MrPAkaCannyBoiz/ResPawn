using ApiContracts.Dtos;
using ReSpawnMarket.SDK.ServiceInterfaces;
using System.Text.Json;

namespace BlazorApp.Services;

public class HttpUploadProductService : IUploadProductService
{
    private readonly HttpClient client;

    public HttpUploadProductService(HttpClient client)
    {
        this.client = client;
    }

    public async Task<ProductDto> UploadProductAsync(int customerId, UploadProductDto request)
    {
       var apiRequest = await client.PostAsJsonAsync($"api/products/customers/{customerId}", request);
       string apiStringResponse = await apiRequest.Content.ReadAsStringAsync();
       if (!apiRequest.IsSuccessStatusCode)
       {
           throw new Exception($"Error uploading product: {apiStringResponse}");
       }
       ProductDto productDto = JsonSerializer
            .Deserialize<ProductDto>(apiStringResponse, makeJsonCaseInsensitive())!;
       return productDto;
    }



    private JsonSerializerOptions makeJsonCaseInsensitive()
    {
        JsonSerializerOptions jsonSerializerOptions = new JsonSerializerOptions
        {
            PropertyNameCaseInsensitive = true
        };
        return jsonSerializerOptions;
    }
}
