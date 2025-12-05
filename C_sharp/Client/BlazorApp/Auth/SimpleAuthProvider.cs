using ApiContracts.Dtos;
using BlazorApp.Services;
using Microsoft.AspNetCore.Components.Authorization;
using Microsoft.JSInterop;
using System.Security.Claims;
using System.Text.Json;

namespace BlazorApp.Auth;

public class SimpleAuthProvider : AuthenticationStateProvider
{
    private readonly HttpClient _httpClient;
    private ClaimsPrincipal _currentClaimsPrincipal;
    private readonly IJSRuntime _jSRuntime;
    private string? _primaryCacheUserJson; // primary cache for current customer json

    public SimpleAuthProvider(HttpClient httpClient, IJSRuntime jSRuntime)
    {
        _httpClient = httpClient;
        _jSRuntime = jSRuntime;
    }

    public async Task CustomerLoginAsync(string email, string password)
    {
        HttpResponseMessage response = await _httpClient.PostAsJsonAsync("customers/login", 
            new CustomerLoginDto() 
            { 
                Email = email, 
                Password = password 
            });
        string content = await response.Content.ReadAsStringAsync();
        if (!response.IsSuccessStatusCode)
        {
            throw new Exception($"Login failed: {response.StatusCode}, {content}");
        }

        CustomerLoginResponseDto responseDto = JsonSerializer.Deserialize<CustomerLoginResponseDto>(
            content, JsonCaseInsensitiveExtension.MakeJsonCaseInsensitive())!;

        string serializedData = JsonSerializer.Serialize(responseDto);
        await _jSRuntime.InvokeVoidAsync("sessionStorage.setItem", "currentCustomer", serializedData);
        _primaryCacheUserJson = serializedData;

        List<Claim> claims = new()
        {
            // claim should be unique (depend what we have in database)
            new Claim(ClaimTypes.NameIdentifier, responseDto.CustomerId.ToString()),
            new Claim(ClaimTypes.Email, responseDto.Email),
            new Claim(ClaimTypes.Name, responseDto.FirstName + " " + responseDto.LastName),
            new Claim("CanSell", responseDto.CanSell.ToString()) // custom claim for selling permission
        };

        ClaimsIdentity identity = new(claims, "customerapiauth");
        _currentClaimsPrincipal = new ClaimsPrincipal(identity);

        // Notify the authentication state has changed, then Blazor will update the UI accordingly.
        NotifyAuthenticationStateChanged(Task.FromResult(new AuthenticationState(_currentClaimsPrincipal)));
    }

    public override async Task<AuthenticationState> GetAuthenticationStateAsync()
    {
        string? customerAsJson = _primaryCacheUserJson;
        try
        {
            customerAsJson = await _jSRuntime.InvokeAsync<string>("sessionStorage.getItem", "currentCustomer");
            _primaryCacheUserJson = customerAsJson;
        }
        catch (InvalidOperationException)
        {
            var emptyState = new AuthenticationState(new());
            return emptyState;
        }
        if (string.IsNullOrEmpty(customerAsJson))
        {
            var emptyState = new AuthenticationState(new());
            return emptyState;
        }

        CustomerLoginResponseDto? customerDto = JsonSerializer.Deserialize<CustomerLoginResponseDto>(
            customerAsJson, JsonCaseInsensitiveExtension.MakeJsonCaseInsensitive())!;
        List<Claim> claims = new()
        {
            new Claim(ClaimTypes.NameIdentifier, customerDto.CustomerId.ToString()),
            new Claim(ClaimTypes.Email, customerDto.Email),
            new Claim(ClaimTypes.Name, customerDto.FirstName + " " + customerDto.LastName),
            new Claim("CanSell", customerDto.CanSell.ToString())
        };
        ClaimsIdentity identity = new(claims, "customerapiauth");
        ClaimsPrincipal principal = new(identity);
        AuthenticationState authState = new(principal);
        return authState;
    }

    public async Task CustomerLogoutAsync()
    {
        await _jSRuntime.InvokeVoidAsync("sessionStorage.setItem", "currentCustomer", "");
        _primaryCacheUserJson = null;
        _currentClaimsPrincipal = new();
        NotifyAuthenticationStateChanged(Task.FromResult(new AuthenticationState(_currentClaimsPrincipal)));
    }

    public async Task UpdateCurrentCustomerCanSellAsync(bool canSell)
    {
        // Read current customer session
        var customerJson = _primaryCacheUserJson
            ?? await _jSRuntime.InvokeAsync<string>("sessionStorage.getItem", "currentCustomer");
        if (string.IsNullOrWhiteSpace(customerJson))
        {
            // No session, nothing to update
            return;
        }

        var dto = JsonSerializer.Deserialize<CustomerLoginResponseDto>(
            customerJson, JsonCaseInsensitiveExtension.MakeJsonCaseInsensitive());
        if (dto is null) return;

        // Update local DTO and persist to sessionStorage via JSRuntime
        dto.CanSell = canSell;
        var updatedJson = JsonSerializer.Serialize(dto);
        await _jSRuntime.InvokeVoidAsync("sessionStorage.setItem", "currentCustomer", updatedJson);
        _primaryCacheUserJson = updatedJson;

        // Rebuild principal with updated claim
        var claims = new List<Claim>
    {
        new Claim(ClaimTypes.NameIdentifier, dto.CustomerId.ToString()),
        new Claim(ClaimTypes.Email, dto.Email),
        new Claim(ClaimTypes.Name, dto.FirstName + " " + dto.LastName),
        new Claim("CanSell", dto.CanSell.ToString())
    };
        var identity = new ClaimsIdentity(claims, "customerapiauth");
        _currentClaimsPrincipal = new ClaimsPrincipal(identity);

        // Notify Blazor to refresh UI immediately
        NotifyAuthenticationStateChanged(Task.FromResult(new AuthenticationState(_currentClaimsPrincipal)));
    }
}
