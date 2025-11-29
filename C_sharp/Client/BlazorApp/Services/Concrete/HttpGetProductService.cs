using ApiContracts.Dtos;
using BlazorApp.Services.Interface;
using System.Text.Json;

namespace BlazorApp.Services.Concrete;

public class HttpGetProductService : IGetProductService
{
    private readonly HttpClient _httpClient;
    public HttpGetProductService(HttpClient httpClient)
    {
        _httpClient = httpClient;
    }
    public async Task<List<ProductDto>> GetAllAsync()
    {
       var httpResponse = await _httpClient.GetAsync("api/products");
       var textResponse = await httpResponse.Content.ReadAsStringAsync();
       return JsonSerializer.Deserialize<List<ProductDto>>(textResponse, 
           JsonCaseInsensitiveExtension.MakeJsonCaseInsensitive())!;
    }

    public async Task<List<ProductDto>> GetAllPendingAsync()
    {
        var httpResponse = await _httpClient.GetAsync("api/products/pending");
        var textResponse = await httpResponse.Content.ReadAsStringAsync();
        return JsonSerializer.Deserialize<List<ProductDto>>(textResponse,
            JsonCaseInsensitiveExtension.MakeJsonCaseInsensitive())!;
    }

    public async Task<DetailedProductDto> GetSingleAsync(int id)
    {
       var httpResponse = await _httpClient.GetAsync($"api/products/{id}");
       var textResponse = await httpResponse.Content.ReadAsStringAsync();
       return JsonSerializer.Deserialize<DetailedProductDto>(textResponse,
              JsonCaseInsensitiveExtension.MakeJsonCaseInsensitive())!;
    }
}
