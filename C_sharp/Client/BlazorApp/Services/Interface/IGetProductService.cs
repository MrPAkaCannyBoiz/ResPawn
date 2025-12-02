using ApiContracts.Dtos;

namespace BlazorApp.Services.Interface;

public interface IGetProductService
{
    public Task<DetailedProductDto> GetSingleAsync(int id);
    public Task<IQueryable<ProductWithFirstImageDto>> GetAllAsync();
    public Task<IQueryable<ProductWithFirstImageDto>> GetAllPendingAsync();
    public Task<IQueryable<ProductWithFirstImageDto>> GetAllAvailableAsync();
}
