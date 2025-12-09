using ApiContracts;
using ApiContracts.Dtos;
using System.Security.Claims;

namespace BlazorApp.Services.Interface
{
    public interface IResellerAuthService
    {
        public Task<ResellerLoginResponseDto> LoginResellerAsync(ResellerLoginDto dto);
        public Task LogoutResellerAsync();
        public Task<ClaimsPrincipal> GetAuthAsync();
        public Action<ClaimsPrincipal> OnAuthStateChanged { get; set; }
    }
}
