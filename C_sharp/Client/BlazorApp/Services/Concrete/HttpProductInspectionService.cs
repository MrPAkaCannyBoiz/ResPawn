using System;
using System.Text.Json;
using ApiContracts.Dtos;
using BlazorApp.Services.Interface;

namespace BlazorApp.Services.Concrete;

public class HttpProductInspectionService : IProductInspectionService
{
    private readonly HttpClient client;

    private  readonly JsonSerializerOptions JsonOpts = new()
    {
        PropertyNameCaseInsensitive = true
    };

    public HttpProductInspectionService(HttpClient client)
    {
        this.client = client;
    }
    public async Task<IReadOnlyList<ProductDto>> GetPendingProductsAsync()
    {
      var http = await client.GetAsync("products/pending");
        var text = await http.Content.ReadAsStringAsync();

        if (!http.IsSuccessStatusCode)
        {
            throw new Exception(string.IsNullOrWhiteSpace(text)
                ? $"Request failed: {(int)http.StatusCode} {http.ReasonPhrase}"
                : text);
        }

        var result = JsonSerializer.Deserialize<List<ProductDto>>(text, JsonOpts);
        return result ?? new List<ProductDto>();
    }

    public async Task<DetailedProductDto> GetProductDetailsAsync(int productId)
    {
        var http = await client.GetAsync($"products/{productId}");
        var text = await http.Content.ReadAsStringAsync();

        if (!http.IsSuccessStatusCode)
        {
            throw new Exception(string.IsNullOrWhiteSpace(text)
                ? $"Request failed: {(int)http.StatusCode} {http.ReasonPhrase}"
                : text);
        }

        return JsonSerializer.Deserialize<DetailedProductDto>(text, JsonOpts)
               ?? throw new Exception("Failed to deserialize DetailedProductDto.");
    }

    public async Task<ProductInspectionResultDto> ReviewProductAsync(int productId, ProductInspectionDto dto)
    {
       var http = await client.PostAsJsonAsync(
            $"inspection/product/{productId}",
            dto);

        var text = await http.Content.ReadAsStringAsync();

        if (!http.IsSuccessStatusCode)
        {
            throw new Exception(string.IsNullOrWhiteSpace(text)
                ? $"Request failed: {(int)http.StatusCode} {http.ReasonPhrase}"
                : text);
        }

        return JsonSerializer.Deserialize<ProductInspectionResultDto>(text, JsonOpts)
               ?? throw new Exception("Failed to deserialize ProductInspectionResultDto.");
    }
    }

