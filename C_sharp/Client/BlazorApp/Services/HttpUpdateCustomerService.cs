using ApiContracts.Dtos;
using System.Text.Json;

namespace BlazorApp.Services;

public class HttpUpdateCustomerService : IUpdateCustomerService
{
    private readonly HttpClient _client;

    public HttpUpdateCustomerService(HttpClient client)
    {
        this._client = client;
    }

    public async Task<CustomerDto> UpdateCustomerAsync(int id, UpdateCustomerDto request)
    {
        var apiRequest = await _client.PatchAsJsonAsync($"api/customers/{id}", request);
        string apiStringResponse = await apiRequest.Content.ReadAsStringAsync();
        if (!apiRequest.IsSuccessStatusCode)
        {
            throw new Exception($"Error updating customer: {apiStringResponse}");
        }
        var CustomerDto = JsonSerializer.Deserialize<CustomerDto>(apiStringResponse,
            JsonCaseInsensitiveExtension.MakeJsonCaseInsensitive())!;
        return CustomerDto;
    }
}
