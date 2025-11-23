using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

using System.Text.Json;

namespace BlazorApp.Services;

public class HttpProductInspectionService // : IProductInspectionHttpService
{
    private readonly HttpClient client;

    public HttpProductInspectionService(HttpClient client)
    {
        this.client = client;
    }

    //public async Task<ICollection<ProductDto>> GetPendingProductsAsync()
    //{
    //    var http = await client.GetAsync("api/product-inspection/pending");
    //    var text = await http.Content.ReadAsStringAsync();
    //    return JsonSerializer.Deserialize<List<ProductDto>>(text, new JsonSerializerOptions { PropertyNameCaseInsensitive = true })!;
    //}

    //public async Task<ProductInspectionResultDto> ReviewProductAsync(ProductInspectionRequestDto dto)
    //{
    //    var http = await client.PostAsJsonAsync("api/product-inspection/review", dto);
    //    var text = await http.Content.ReadAsStringAsync();
    //    return JsonSerializer.Deserialize<ProductInspectionResultDto>(text, new JsonSerializerOptions { PropertyNameCaseInsensitive = true })!;
    //}
}
