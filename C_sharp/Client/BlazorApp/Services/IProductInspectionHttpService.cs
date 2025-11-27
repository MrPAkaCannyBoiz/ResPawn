using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using ApiContracts.Dtos;


namespace BlazorApp.Services;

public interface IProductInspectionHttpService
{
    Task<ICollection<ProductDto>> GetPendingProductsAsync();
    Task<ProductInspectionResultDto> ReviewProductAsync(ProductInspectionRequestDto dto);
}
