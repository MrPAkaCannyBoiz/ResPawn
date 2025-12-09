using ApiContracts;
using ApiContracts.Dtos;
using BlazorApp.Services;
using BlazorApp.Services.Interface;
using Microsoft.AspNetCore.Components.Authorization;
using Microsoft.JSInterop;
using System.Net.Http;
using System.Security.Claims;
using System.Text.Json;

namespace BlazorApp.Auth;

public class CustomAuthProvider : AuthenticationStateProvider
{
    private readonly IAuthService _authService; // Jwt auth service
    // non JWT auth service can also be used here
    private readonly HttpClient _httpClient;
    private ClaimsPrincipal _currentClaimsPrincipal;
    private readonly IJSRuntime _jSRuntime;
    private string? _primaryCacheUserJson; // primary cache for current customer json

    public CustomAuthProvider(IAuthService authService, HttpClient httpClient, IJSRuntime jSRuntime)
    {
        _authService = authService;
        _authService.OnAuthStateChanged += AuthStateChanged;

        // non JWT auth service can also be used here
        _httpClient = httpClient;
        _jSRuntime = jSRuntime;
    }

    public override async Task<AuthenticationState> GetAuthenticationStateAsync()
    {
        ClaimsPrincipal principal = await _authService.GetAuthAsync();
        return new AuthenticationState(principal);
    }

    private void AuthStateChanged(ClaimsPrincipal principal)
    {
        NotifyAuthenticationStateChanged(
            Task.FromResult(
                new AuthenticationState(principal)
            )
        );
    }

    // Reseller login method (non JWT based)
    public async Task ResellerLoginAsync(string username, string password)
    {
        HttpResponseMessage response = await _httpClient.PostAsJsonAsync("reseller/login",
         new ResellerLoginDto()
         {
             Username = username,
             Password = password
         });
        string content = await response.Content.ReadAsStringAsync();
        if (!response.IsSuccessStatusCode)
        {
            throw new Exception($"Login failed: {response.StatusCode}, {content}");
        }

        ResellerLoginResponseDto responseDto = JsonSerializer.Deserialize<ResellerLoginResponseDto>(
            content, JsonCaseInsensitiveExtension.MakeJsonCaseInsensitive())!;

        string serializedData = JsonSerializer.Serialize(responseDto);
        await _jSRuntime.InvokeVoidAsync("sessionStorage.setItem", "currentReseller", serializedData);
        _primaryCacheUserJson = serializedData;

        List<Claim> claims = new()
        {
            // claim should be unique (depend what we have in database)
            new Claim(ClaimTypes.NameIdentifier, responseDto.Id.ToString()),
            new Claim(ClaimTypes.Name, responseDto.Username),
            new Claim(ClaimTypes.Role, "Reseller")
        };

        ClaimsIdentity identity = new(claims, "resellerapiauth");
        _currentClaimsPrincipal = new ClaimsPrincipal(identity);

        // Notify the authentication state has changed, then Blazor will update the UI accordingly.
        NotifyAuthenticationStateChanged(Task.FromResult(new AuthenticationState(_currentClaimsPrincipal)));

    }
    public async Task ResellerLogoutAsync()
    {
        await _jSRuntime.InvokeVoidAsync("sessionStorage.setItem", "currentReseller", "");
        _primaryCacheUserJson = null;
        _currentClaimsPrincipal = new ClaimsPrincipal();
        NotifyAuthenticationStateChanged(Task.FromResult(new AuthenticationState(_currentClaimsPrincipal)));
    }

 



}
