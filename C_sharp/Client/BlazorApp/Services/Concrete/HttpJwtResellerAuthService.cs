using ApiContracts;
using ApiContracts.Dtos;
using BlazorApp.Services.Interface;
using Microsoft.JSInterop;
using System.Net;
using System.Security.Claims;
using System.Text.Json;
using WebAPI.ClaimFactoriesExtension;
using WebAPI.ClaimFactoriesExtensions;

namespace BlazorApp.Services.Concrete
{
    public class HttpJwtResellerAuthService : IResellerAuthService
    {
        private HttpClient _httpClient;
        private IJSRuntime _jsRuntime;
        public Action<ClaimsPrincipal> OnAuthStateChanged { get; set; } = null!;

        public string Jwt { get; private set; } = string.Empty; // public getter, private setter by can the chinese

        public HttpJwtResellerAuthService(HttpClient _httpClient, IJSRuntime _jsRuntime)
        {
            this._jsRuntime = _jsRuntime;
            this._httpClient = _httpClient;
        }
        public async Task<ResellerLoginResponseDto> LoginResellerAsync(ResellerLoginDto dto)
        {
            HttpResponseMessage http = await _httpClient
            .PostAsJsonAsync("api/reseller/login", dto);
            string response = await http.Content.ReadAsStringAsync(); 
            if (!http.IsSuccessStatusCode)
            {
                throw new Exception($"Login failed: {response}");
            }
            ResellerLoginResponseDto responseDto = JsonSerializer.Deserialize<ResellerLoginResponseDto>(response,
                JsonCaseInsensitiveExtension.MakeJsonCaseInsensitive())!;
            if (responseDto is null || string.IsNullOrWhiteSpace(responseDto.Jwt))
            {
                throw new Exception("Login succeeded but JWT token is missing in response.");
            }
            //Jwt = responseDto.JwtToken; // Deserialize to get only the jwt token
            ClaimsPrincipal principal = await GetAuthAsync();
            OnAuthStateChanged.Invoke(principal);

            return responseDto;
        }

        public async Task LogoutResellerAsync()
        {
            await _httpClient.PostAsync("api/reseller/logout", null);
            OnAuthStateChanged.Invoke(new());
        }
    
  public async Task<ClaimsPrincipal> GetAuthAsync()
        {
            HttpResponseMessage http = await _httpClient.GetAsync("api/reseller/claims");
            string response = await http.Content.ReadAsStringAsync(); // this is Serialized
            if (http.StatusCode == HttpStatusCode.Unauthorized)
            {
                return new ClaimsPrincipal();
            }
            else if (!http.IsSuccessStatusCode)
            {
                throw new Exception($"AuthCookie fetch failed: {http.StatusCode} - {response}");
            }
            var claimsDto = await http.Content.ReadFromJsonAsync<ResellerClaimDto>();
            var identity = new ClaimsIdentity(ResellerClaimsExtention.ToClaims(claimsDto!), "jwt");
            var principal = new ClaimsPrincipal(identity);
            if (OnAuthStateChanged != null)
            {
                OnAuthStateChanged.Invoke(new ClaimsPrincipal(identity));
            }
            else
            {
                throw new Exception("OnAuthStateChanged is null");
            }
            return principal;
        }
    }
}
