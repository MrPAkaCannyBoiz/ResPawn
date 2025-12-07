using ApiContracts.Dtos;
using System.Security.Claims;

namespace BlazorApp.Services.Interface;

public interface ICustomerAuthService
{
    public Task<CustomerLoginResponseDto> LoginAsync(CustomerLoginDto dto);
    public Task LogoutAsync();
    public Task<ClaimsPrincipal> GetAuthAsync();
    public Action<ClaimsPrincipal> OnAuthStateChanged { get; set; }
}
