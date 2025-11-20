using System.Text.Json;
using System.Text.Json.Serialization;
using ApiContracts.Dtos;
using Microsoft.AspNetCore.Mvc;

namespace BlazorApp.Services;

public class HttpCustomerService : ICustomerServices
{
    private readonly HttpClient client;
    private static readonly JsonSerializerOptions JsonOpts =
        new() { PropertyNameCaseInsensitive = true };

    public HttpCustomerService(HttpClient client)
    {
        this.client = client;
    }

   
    private sealed class RegisterCustomerResponseDto
    {
        [JsonPropertyName("customerId")]
        public int CustomerId { get; set; }

        [JsonPropertyName("message")]
        public string? Message { get; set; }
    }

 public async Task<CustomerDto> AddCustomerAsync(CreateCustomerDto request)
    {
        var http = await client.PostAsJsonAsync("api/customers", request);
        var text = await http.Content.ReadAsStringAsync();

        if (!http.IsSuccessStatusCode)
            throw new Exception(string.IsNullOrWhiteSpace(text)
                ? $"Request failed: {(int)http.StatusCode} {http.ReasonPhrase}"
                : text);

        return JsonSerializer.Deserialize<CustomerDto>(text, new JsonSerializerOptions
        {
            PropertyNameCaseInsensitive = true
        })!;
    }

    public async Task<CustomerDto> GetSingleAsync(int id)
    {
        var http = await client.GetAsync($"api/customers/{id}");
        var text = await http.Content.ReadAsStringAsync();

        if (!http.IsSuccessStatusCode)
            throw new Exception(string.IsNullOrWhiteSpace(text)
                ? $"Request failed: {(int)http.StatusCode} {http.ReasonPhrase}"
                : text);

        return JsonSerializer.Deserialize<CustomerDto>(text, new JsonSerializerOptions
        {
            PropertyNameCaseInsensitive = true
        })!;
    }
}
