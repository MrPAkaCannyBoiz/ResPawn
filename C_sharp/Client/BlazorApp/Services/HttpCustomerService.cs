using System.Text.Json;
using System.Text.Json.Serialization;
using ApiContracts;

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
        
        var httpResponse = await client.PostAsJsonAsync(
            "service/CustomerRegisterService/RegisterCustomer", request);

        var json = await httpResponse.Content.ReadAsStringAsync();
        if (!httpResponse.IsSuccessStatusCode)
            throw new Exception(json);

      
        var grpcResp = JsonSerializer.Deserialize<RegisterCustomerResponseDto>(json, JsonOpts)
                      ?? throw new Exception("Empty response");

     
        return new CustomerDto
        {
            Id           = grpcResp.CustomerId,   
            FirstName    = request.FirstName,
            LastName     = request.LastName,
            Email        = request.Email,
            PhoneNumber  = request.PhoneNumber,
            StreetName   = request.StreetName,
            SecondaryUnit = request.SecondaryUnit,
            PostalCode   = request.PostalCode,
            City         = request.City
        };
    }

    public async Task<CustomerDto> GetSingleAsync(int id)
    {
       var httpResponse = await client.GetAsync($"service/GetCustomerService/{id}");
    var text = await httpResponse.Content.ReadAsStringAsync();

    if (!httpResponse.IsSuccessStatusCode)
        throw new Exception(string.IsNullOrWhiteSpace(text)
            ? $"Request failed: {(int)httpResponse.StatusCode} {httpResponse.ReasonPhrase}"
            : text);

    return JsonSerializer.Deserialize<CustomerDto>(text, new JsonSerializerOptions
    {
        PropertyNameCaseInsensitive = true
    })!;
    }
}
