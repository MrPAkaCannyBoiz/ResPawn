using ApiContracts.Dtos;

namespace BlazorApp.Services.Interface;

public interface IGetProductService
{
    public Task<DetailedProductDto> GetSingleAsync(int id);
    public Task<IQueryable<ProductWithFirstImageDto>> GetAllAsync();
    public Task<IQueryable<ProductWithFirstImageDto>> GetAllPendingAsync();
    public Task<IQueryable<ProductWithFirstImageDto>> GetAllAvailableAsync();
    public Task<IQueryable<ProductWithFirstImageDto>> GetAllReviewingAsync();

    public Task<List<ProductWithFirstImageDto>> GetCustomerProductsAsync(int customerId); // TODO: delete later (Can said)
    public Task<IQueryable<LatestProductFromInspectionDto>> GetAllLatestAsync(int id);
}
