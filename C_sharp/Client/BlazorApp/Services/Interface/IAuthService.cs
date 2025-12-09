using ApiContracts.Dtos;
using Microsoft.AspNetCore.Components.Authorization;
using System.Security.Claims;

namespace BlazorApp.Services.Interface;

public interface IAuthService
{
    public Task<CustomerLoginResponseDto> LoginAsync(CustomerLoginDto dto);
    public Task LogoutAsync();
    public Task<ClaimsPrincipal> GetAuthAsync();
    public Action<ClaimsPrincipal> OnAuthStateChanged { get; set; }
   
}
