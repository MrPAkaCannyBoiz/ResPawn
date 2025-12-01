using ApiContracts.Dtos;

namespace BlazorApp.Services.Interface;

public interface IUploadProductService
{
    public Task<ProductDto> UploadProductAsync(int customerId, UploadProductDto request);
}
