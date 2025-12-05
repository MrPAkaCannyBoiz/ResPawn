using ApiContracts.Dtos;
using BlazorApp.Services.Interface;

namespace BlazorApp.Services.Concrete;

public class HttpGetCustomerService : IGetCustomerService
{
    private readonly HttpClient client;
    public HttpGetCustomerService(HttpClient client)
    {
        this.client = client;
    }
    public async Task<List<CustomerDto>> GetAllCustomersAsync()
    {
        HttpResponseMessage http = await client.GetAsync("api/customers");
        string responseBody = await http.Content.ReadAsStringAsync();
        if (!http.IsSuccessStatusCode)
        {
            throw new Exception($"Error retrieving customers: {responseBody}");
        }
        List<CustomerDto> customers = System.Text.Json.JsonSerializer.Deserialize<List<CustomerDto>>(
            responseBody, JsonCaseInsensitiveExtension.MakeJsonCaseInsensitive())!;
        return customers;
    }

    public async Task<CustomerDto> GetCustomerByIdAsync(int customerId)
    {
        HttpResponseMessage http = await client.GetAsync($"api/customers/{customerId}");
        string responseBody = await http.Content.ReadAsStringAsync();
        if (!http.IsSuccessStatusCode)
        {
            throw new Exception($"Error retrieving customer with ID {customerId}: {responseBody}");
        }
        CustomerDto customer = System.Text.Json.JsonSerializer.Deserialize<CustomerDto>(
            responseBody, JsonCaseInsensitiveExtension.MakeJsonCaseInsensitive())!;
        return customer;
    }
}
