using ApiContracts.Dtos;
using BlazorApp.Services.Interface;
using Microsoft.JSInterop;
using System.Net;
using System.Security.Claims;
using System.Text.Json;
using WebAPI.ClaimFactoriesExtension;

namespace BlazorApp.Services.Concrete;

public class HttpJwtCustomerAuthLoginService : IAuthService
{
    private HttpClient _httpClient;
    private IJSRuntime _jsRuntime;
    public Action<ClaimsPrincipal> OnAuthStateChanged { get; set; } = null!;

    public string Jwt { get; private set; } = string.Empty; // public getter, private setter
    public HttpJwtCustomerAuthLoginService(HttpClient httpClient, IJSRuntime jsRuntime)
    {
        _httpClient = httpClient;
        _jsRuntime = jsRuntime;
    }
    
    public async Task<CustomerLoginResponseDto> LoginAsync(CustomerLoginDto dto)
    {
        HttpResponseMessage http = await _httpClient
            .PostAsJsonAsync("api/customers/login", dto);
        string response = await http.Content.ReadAsStringAsync(); // this is Serialized
        if (!http.IsSuccessStatusCode)
        {
            throw new Exception($"Login failed: {response}");
        }
        CustomerLoginResponseDto responseDto = JsonSerializer.Deserialize<CustomerLoginResponseDto>(response,
            JsonCaseInsensitiveExtension.MakeJsonCaseInsensitive())!;
        if (responseDto is null || string.IsNullOrWhiteSpace(responseDto.JwtToken))
        {
            throw new Exception("Login succeeded but JWT token is missing in response.");
        }
        //Jwt = responseDto.JwtToken; // Deserialize to get only the jwt token
        ClaimsPrincipal principal = await GetAuthAsync();
        OnAuthStateChanged.Invoke(principal);
        
        return responseDto;
    }

    public async Task LogoutAsync()
    {
        await _httpClient.PostAsync("api/customers/logout", null);
        OnAuthStateChanged.Invoke(new());
    }

    public async Task<ClaimsPrincipal> GetAuthAsync()
    {
        HttpResponseMessage http = await _httpClient.GetAsync("api/customers/claims");
        string response = await http.Content.ReadAsStringAsync(); // this is Serialized
        if (http.StatusCode == HttpStatusCode.Unauthorized)
        {
            return new ClaimsPrincipal();
        }
        else if (!http.IsSuccessStatusCode)
        {
            throw new Exception($"AuthCookie fetch failed: {http.StatusCode} - {response}");
        }
        var claimsDto = await http.Content.ReadFromJsonAsync<CustomerClaimDto>();
        var identity = new ClaimsIdentity(CustomerClaimsExtension.ToClaims(claimsDto!), "jwt");
        var principal = new ClaimsPrincipal(identity);
        OnAuthStateChanged.Invoke(new ClaimsPrincipal(identity));
        return principal;
    }


}
