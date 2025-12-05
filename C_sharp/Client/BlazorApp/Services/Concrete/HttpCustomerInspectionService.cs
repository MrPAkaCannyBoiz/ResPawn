using ApiContracts.Dtos;
using BlazorApp.Services.Interface;

namespace BlazorApp.Services.Concrete;

public class HttpCustomerInspectionService : ICustomerInspectionService
{
    private readonly HttpClient _client;

    public HttpCustomerInspectionService(HttpClient client)
    {
        this._client = client;
    }
    public async Task<EnableSellDto> SetCanSellAsync(int customerId, EnableSellDto dto)
    {
        HttpResponseMessage http = await _client
            .PatchAsJsonAsync($"api/customer/inspection/{customerId}", dto);
        string text = await http.Content.ReadAsStringAsync();
        if (!http.IsSuccessStatusCode)
        {
            throw new Exception($"Error enable/disable sell: {text}");
        }
        EnableSellDto result = System.Text.Json.JsonSerializer
            .Deserialize<EnableSellDto>(text,JsonCaseInsensitiveExtension.MakeJsonCaseInsensitive())!;
        return result;
    }
}
