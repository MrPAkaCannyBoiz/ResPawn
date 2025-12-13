using ApiContracts.Dtos;
using BlazorApp.Services.Interface;
using System.Net;
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
       HttpResponseMessage httpResponse = await _httpClient.GetAsync("api/products");
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
        HttpResponseMessage httpResponse = await _httpClient.GetAsync("api/products/pending");
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
       HttpResponseMessage httpResponse = await _httpClient.GetAsync($"api/products/{id}");
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
        HttpResponseMessage httpResponse = await _httpClient.GetAsync("api/products/available");
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
        HttpResponseMessage httpResponse = await _httpClient.GetAsync("api/products/reviewing");
        string textResponse = await httpResponse.Content.ReadAsStringAsync();

        if (!httpResponse.IsSuccessStatusCode)
        {
            throw new Exception($"Error getting products: {httpResponse.StatusCode}, {textResponse}");
        }

        if (string.IsNullOrWhiteSpace(textResponse))
        {
            return new List<ProductWithFirstImageDto>().AsQueryable();
        }

        List<ProductWithFirstImageDto> productDtos = JsonSerializer
            .Deserialize<List<ProductWithFirstImageDto>>(textResponse,
            JsonCaseInsensitiveExtension.MakeJsonCaseInsensitive())!;

        return productDtos.AsQueryable();
    }

    public async Task<List<ProductWithFirstImageDto>> GetCustomerProductsAsync(int customerId)
    {
        //testisg
         HttpResponseMessage httpResponse =
        await _httpClient.GetAsync($"api/products/customer/{customerId}");

    // If the API returns 404, interpret it as "no products for this customer"
    if (httpResponse.StatusCode == HttpStatusCode.NotFound)
    {
        return new List<ProductWithFirstImageDto>();
    }

    string textResponse = await httpResponse.Content.ReadAsStringAsync();

    if (!httpResponse.IsSuccessStatusCode)
    {
        throw new Exception(
            $"Error getting customer products: {httpResponse.StatusCode}, {textResponse}");
    }

    var products = JsonSerializer.Deserialize<List<ProductWithFirstImageDto>>(
        textResponse,
        JsonCaseInsensitiveExtension.MakeJsonCaseInsensitive()
    );

    return products ?? new List<ProductWithFirstImageDto>();
    }

    public async Task<IQueryable<LatestProductFromInspectionDto>> GetAllLatestAsync(int id)
    {
        HttpResponseMessage httpResponse = await _httpClient.GetAsync($"api/products/inspection/customer/{id}");
        string textResponse = await httpResponse.Content.ReadAsStringAsync();

        if (!httpResponse.IsSuccessStatusCode)
        {
            throw new Exception($"Error getting products: {httpResponse.StatusCode}, {textResponse}");
        }
        if (string.IsNullOrWhiteSpace(textResponse))
        {
            return new List<LatestProductFromInspectionDto>().AsQueryable();
        }

        List<LatestProductFromInspectionDto> productDtos = JsonSerializer
            .Deserialize<List<LatestProductFromInspectionDto>>(textResponse,
            JsonCaseInsensitiveExtension.MakeJsonCaseInsensitive())!;

        return productDtos.AsQueryable();
    }
}
