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
    public async Task<IQueryable<ProductWithFirstImageDto>> GetAllAsync()
    {
       HttpResponseMessage httpResponse = await _httpClient.GetAsync("products");
       string textResponse = await httpResponse.Content.ReadAsStringAsync();
       if (!httpResponse.IsSuccessStatusCode)
       {
            throw new Exception($"Error getting posts: {httpResponse.StatusCode}, {textResponse}");
       }
       List<ProductWithFirstImageDto> productDtos = JsonSerializer
            .Deserialize<List<ProductWithFirstImageDto>>(textResponse, 
           JsonCaseInsensitiveExtension.MakeJsonCaseInsensitive())!;
       return productDtos.AsQueryable();
    }

    public async Task<IQueryable<ProductWithFirstImageDto>> GetAllPendingAsync()
    {
        HttpResponseMessage httpResponse = await _httpClient.GetAsync("products/pending");
        string textResponse = await httpResponse.Content.ReadAsStringAsync();
        if (!httpResponse.IsSuccessStatusCode)
        {
            throw new Exception($"Error getting posts: {httpResponse.StatusCode}, {textResponse}");
        }
        List<ProductWithFirstImageDto> productDtos = JsonSerializer
          .Deserialize<List<ProductWithFirstImageDto>>(textResponse,
         JsonCaseInsensitiveExtension.MakeJsonCaseInsensitive())!;
        return productDtos.AsQueryable();
    }

    public async Task<DetailedProductDto> GetSingleAsync(int id)
    {
       HttpResponseMessage httpResponse = await _httpClient.GetAsync($"products/{id}");
       string textResponse = await httpResponse.Content.ReadAsStringAsync();
       if (!httpResponse.IsSuccessStatusCode)
       {
           throw new Exception($"Error getting posts: {httpResponse.StatusCode}, {textResponse}");
       }
        return JsonSerializer.Deserialize<DetailedProductDto>(textResponse,
              JsonCaseInsensitiveExtension.MakeJsonCaseInsensitive())!;
    }
    public async Task<IQueryable<ProductWithFirstImageDto>> GetAllAvailableAsync()
    {
        HttpResponseMessage httpResponse = await _httpClient.GetAsync("products/available");
        string textResponse = await httpResponse.Content.ReadAsStringAsync();

        if (!httpResponse.IsSuccessStatusCode)
        {
            throw new Exception($"Error getting products: {httpResponse.StatusCode}, {textResponse}");
        }

        if (string.IsNullOrWhiteSpace(textResponse))
        {
            // Return empty list if no products are available
            return new List<ProductWithFirstImageDto>().AsQueryable();
        }

        List<ProductWithFirstImageDto> productDtos = JsonSerializer
            .Deserialize<List<ProductWithFirstImageDto>>(textResponse,
            JsonCaseInsensitiveExtension.MakeJsonCaseInsensitive())!;

        return productDtos.AsQueryable();
    }

    public async Task<IQueryable<ProductWithFirstImageDto>> GetAllReviewingAsync()
    {
        HttpResponseMessage httpResponse = await _httpClient.GetAsync("products/reviewing");
        string textResponse = await httpResponse.Content.ReadAsStringAsync();

        if (!httpResponse.IsSuccessStatusCode)
        {
            throw new Exception($"Error getting products: {httpResponse.StatusCode}, {textResponse}");
        }

        if (string.IsNullOrWhiteSpace(textResponse))
        {
            // Return empty list if no products are available
            return new List<ProductWithFirstImageDto>().AsQueryable();
        }

        List<ProductWithFirstImageDto> productDtos = JsonSerializer
            .Deserialize<List<ProductWithFirstImageDto>>(textResponse,
            JsonCaseInsensitiveExtension.MakeJsonCaseInsensitive())!;

        return productDtos.AsQueryable();
    }
}
