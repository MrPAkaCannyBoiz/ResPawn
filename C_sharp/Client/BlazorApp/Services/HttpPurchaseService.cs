using System;
using System.Text.Json;
using ApiContracts;
using BlazorApp.InterfaceServices;

namespace BlazorApp.Services;

public class HttpPurchaseService : IPurcharseService
{
    private readonly HttpClient client;
    private static readonly JsonSerializerOptions JsonOpts =
        new()
        {
            PropertyNameCaseInsensitive = true
        };

    public HttpPurchaseService(HttpClient client)
    {
        this.client=client;
    }
    public async Task<BuyProductsResultDto> BuyProductsAsync(BuyProductRequestDto request)
    {
        Console.WriteLine("[HttpPurchaseService] POST api/purchases");
        var http = await client.PostAsJsonAsync("api/purchases", request);

         Console.WriteLine($"[HttpPurchaseService] Status: {(int)http.StatusCode} {http.ReasonPhrase}");

        var text = await http.Content.ReadAsStringAsync();
 Console.WriteLine("[HttpPurchaseService] Response body:");
        Console.WriteLine(text);
        if (!http.IsSuccessStatusCode)
            throw new Exception(string.IsNullOrWhiteSpace(text)
                ? $"Request failed: {(int)http.StatusCode} {http.ReasonPhrase}"
                : text);

  var result = JsonSerializer.Deserialize<BuyProductsResultDto>(text, JsonOpts);

    if (result is null)
        throw new Exception("Failed to deserialize BuyProductsResultDto from response.");

    return result;
    }
    }

