using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace ReSpawnMarket.SDK.ServiceInterfaces
{
    public interface IProductInspectionService
    {
         Task<ICollection<ProductDto>> GetPendingProductsAsync();
        Task<ProductInspectionResultDto> ReviewProductAsync(ProductInspectionRequestDto dto);
 
    }
}