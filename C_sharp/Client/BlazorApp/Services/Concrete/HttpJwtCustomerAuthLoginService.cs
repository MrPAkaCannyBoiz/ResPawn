using ApiContracts.Dtos;
using BlazorApp.Services.Interface;
using Microsoft.JSInterop;
using System.Security.Claims;
using System.Text.Json;

namespace BlazorApp.Services.Concrete;

public class HttpJwtCustomerAuthLoginService : ICustomerAuthService
{
    private HttpClient _httpClient;
    private IJSRuntime _jsRuntime;
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
        Jwt = responseDto.JwtToken; // Deserialize to get only the jwt token

        await CacheTokenAsync();
        
        ClaimsPrincipal principal = await CreateClaimsPrincipal();
        OnAuthStateChanged.Invoke(principal);
        
        return responseDto;
    }

    public async Task LogoutAsync()
    {
        await ClearTokenFromCacheAsync();
        _httpClient.DefaultRequestHeaders.Remove("Authorization");
        Jwt = string.Empty;
        ClaimsPrincipal principal = new();
        OnAuthStateChanged.Invoke(principal);
    }
    public Action<ClaimsPrincipal> OnAuthStateChanged { get; set; } = null!;

    public async Task<ClaimsPrincipal> GetAuthAsync()
    {
        // Try to hydrate from JS storage; during prerender this throws, so catch and return empty principal
        try
        {
            return await CreateClaimsPrincipal();
        }
        catch (InvalidOperationException)
        {
            // JS not available yet (prerender). Return anonymous; caller should retry after first render.
            return new ClaimsPrincipal();
        }
    }

    private async Task<string?> GetTokenFromCacheAsync()
    {
        try
        {
            return await _jsRuntime.InvokeAsync<string>("sessionStorage.getItem", "jwt");
        }
        catch (InvalidOperationException)
        {
            // Prerender phase: JS interop not allowed
            return null;
        }
    }

    private async Task CacheTokenAsync()
    {
        try
        {
            await _jsRuntime.InvokeVoidAsync("sessionStorage.setItem", "jwt", Jwt);
        }
        catch (InvalidOperationException)
        {
            // If this ever runs during prerender, ignore; caller should persist after first render.
        }
    }

    private async Task ClearTokenFromCacheAsync()
    {
        try
        {
            await _jsRuntime.InvokeVoidAsync("sessionStorage.setItem", "jwt", "");
        }
        catch (InvalidOperationException)
        {
            // Ignore during prerender
        }
    }

    private async Task<ClaimsPrincipal> CreateClaimsPrincipal()
    {
        var cachedToken = await GetTokenFromCacheAsync();
        if (string.IsNullOrEmpty(Jwt) && string.IsNullOrEmpty(cachedToken))
        {
            return new ClaimsPrincipal();
        }
        if (!string.IsNullOrEmpty(cachedToken))
        {
            Jwt = cachedToken;
        }
        if (!_httpClient.DefaultRequestHeaders.Contains("Authorization"))
        {
            _httpClient.DefaultRequestHeaders.Remove("Authorization");
            _httpClient.DefaultRequestHeaders.Add("Authorization", "Bearer " + Jwt);
        }

        IEnumerable<Claim> claims = ParseClaimsFromJwt(Jwt);
        Console.WriteLine("Parsed Claims: " + claims);

        ClaimsIdentity identity = new(claims, "jwt");

        ClaimsPrincipal principal = new(identity);
        return principal;
    }

    private static IEnumerable<Claim> ParseClaimsFromJwt(string jwt)
    {
        string payload = jwt.Split('.')[1];
        byte[] jsonBytes = ParseBase64WithoutPadding(payload);
        Dictionary<string, object>? keyValuePairs = JsonSerializer.Deserialize<Dictionary<string, object>>(jsonBytes) ?? [];
        return keyValuePairs.Select(kvp =>
        {
            string type = kvp.Key switch
            {
                "given_name" => ClaimTypes.GivenName, //switch to map to standard claim types back
                "family_name" => ClaimTypes.Surname, // i.e. "given_name" and "family_name" are standard JWT claim names
                "nameid" => ClaimTypes.NameIdentifier,
                "email" => ClaimTypes.Email,
                "role" => ClaimTypes.Role,
                _ => kvp.Key
            };
            return new Claim(type, kvp.Value.ToString()!);
        });
    }

    private static byte[] ParseBase64WithoutPadding(string base64)
    {
        switch (base64.Length % 4)
        {
            case 2:
                base64 += "==";
                break;
            case 3:
                base64 += "=";
                break;
        }

        return Convert.FromBase64String(base64);
    }
}
