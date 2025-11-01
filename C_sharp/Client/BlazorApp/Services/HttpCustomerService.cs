using System;
using System.Text.Json;
using ApiContracts;

namespace BlazorApp.Services;

public class HttpCustomerService : ICustomerServices
{
    private readonly HttpClient client;

    public HttpCustomerService(HttpClient client)
    {
        this.client = client;
    }
    public async Task<CustomerDto> AddCustomerAsync(CreateCustomerDto request)
    {
        HttpResponseMessage httpResponseMessage = await client.PostAsJsonAsync("Customer", request);
        string response = await httpResponseMessage.Content.ReadAsStringAsync();
        if(!httpResponseMessage.IsSuccessStatusCode)
        {
            throw new Exception(response);
        }
        return JsonSerializer.Deserialize<CustomerDto>(response, new JsonSerializerOptions
        {
            PropertyNameCaseInsensitive = true
        })!;
    }

    public async Task<CustomerDto> GetSingleAsync(int id)
    {
        HttpResponseMessage httpResponseMessage = await client.GetAsync($"Customer/{id}");
        string response = await httpResponseMessage.Content.ReadAsStringAsync();
        if (!httpResponseMessage.IsSuccessStatusCode)
        {
            throw new Exception(response);
        }
        return JsonSerializer.Deserialize<CustomerDto>(response, new JsonSerializerOptions
        {
            PropertyNameCaseInsensitive = true
        })!;
    }
}
