using System.Net;
using System.Net.Http.Json;
using System.Text.Json;
using ApiContracts.Dtos;
using BlazorApp.Services.Interface; 

namespace BlazorApp.Services.Concrete
{
    public class HttpProductInspectionClient : IProductInspectionClient
    {
        private readonly HttpClient _http;

        public HttpProductInspectionClient(HttpClient http)
        {
            _http = http;
        }
        public async Task<ProductInspectionResultDto?> GetLatestInspectionAsync(
            int productId,
            CancellationToken ct = default)
        {
          
          var url = $"api/inspection/product/{productId}/latest";

        HttpResponseMessage httpResponse = await _http.GetAsync(url, ct);
        if (httpResponse.StatusCode == HttpStatusCode.NotFound)
        {
            return null;
        }
        string textResponse = await httpResponse.Content.ReadAsStringAsync(ct);
        if (!httpResponse.IsSuccessStatusCode)
        {
            throw new Exception(
                $"Error getting latest inspection: {httpResponse.StatusCode}, {textResponse}");
        }

        if (string.IsNullOrWhiteSpace(textResponse))
        {
            return null;
        }

        ProductInspectionResultDto? dto =
            JsonSerializer.Deserialize<ProductInspectionResultDto>(
                textResponse,
                JsonCaseInsensitiveExtension.MakeJsonCaseInsensitive());

        return dto;
        }

        public async Task<ProductInspectionResultDto?> SafeGetLatestInspectionAsync(int productId)
        {
    try
        {
            // Call the existing method. It will throw on 400 or 404.
            return await GetLatestInspectionAsync(productId);
        }
        catch (HttpRequestException ex)
        {
            // 💡 This block specifically catches errors like 400 (Bad Request) or 404 (Not Found).
            // Check the specific status code if needed, but for general fetching errors:
            
            // Log the error for debugging purposes (check your backend/API logs)
            Console.WriteLine($"[Inspection Client Error] Failed to fetch inspection for Product ID {productId}. Request failed with HTTP error: {ex.Message}");
            
            // Return null, indicating no inspection could be loaded, allowing the 
            // calling code (Task.WhenAll) to continue.
            return null;
        }
        catch (Exception ex)
        {
            // Catch any other unexpected error (e.g., deserialization failure)
            Console.WriteLine($"[Inspection Client Error] Unexpected error for Product ID {productId}: {ex.Message}");
            return null;
        }
        }
    }
}
