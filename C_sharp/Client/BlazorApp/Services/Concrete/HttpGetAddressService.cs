using ApiContracts.Dtos;
using BlazorApp.Services.Interface;

namespace BlazorApp.Services.Concrete;

public class HttpGetAddressService : IGetAddressService
{
    private readonly HttpClient client;
    public HttpGetAddressService(HttpClient client)
    {
        this.client = client;
    }
    public async Task<List<PawnshopAddressDto>> GetPawnshopAddressesAsync()
    {
        HttpResponseMessage http = await client.GetAsync("api/addresses/pawnshop");
        string responseBody = await http.Content.ReadAsStringAsync();
        if (!http.IsSuccessStatusCode)
        {
            throw new Exception($"Error retrieving pawnshop addresses: {responseBody}");
        }
        List<PawnshopAddressDto> addresses = System.Text.Json.JsonSerializer.Deserialize<List<PawnshopAddressDto>>(
            responseBody, JsonCaseInsensitiveExtension.MakeJsonCaseInsensitive())!;
        return addresses;
    }
}
