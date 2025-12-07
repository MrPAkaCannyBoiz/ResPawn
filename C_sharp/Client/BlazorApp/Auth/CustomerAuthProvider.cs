using BlazorApp.Services.Interface;
using Microsoft.AspNetCore.Components.Authorization;
using System.Security.Claims;

namespace BlazorApp.Auth;

public class CustomerAuthProvider : AuthenticationStateProvider
{
    private readonly ICustomerAuthService _authService;

    public CustomerAuthProvider(ICustomerAuthService authService)
    {
        _authService = authService;
        _authService.OnAuthStateChanged += AuthStateChanged;
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
}
