using ApiContracts.Dtos;

namespace BlazorApp.Services.Interface;

public interface IGetProductService
{
    public Task<DetailedProductDto> GetSingleAsync(int id);
    public Task<List<ProductDto>> GetAllAsync();
    public Task<List<ProductDto>> GetAllPendingAsync();
}
