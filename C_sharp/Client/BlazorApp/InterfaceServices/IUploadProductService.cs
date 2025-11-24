using ApiContracts.Dtos;

namespace BlazorApp.Services;

public interface IUploadProductService
{
    public Task<ProductDto> UploadProductAsync(int customerId, UploadProductDto request);
}
