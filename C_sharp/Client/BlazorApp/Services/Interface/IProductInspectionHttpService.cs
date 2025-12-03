using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using ApiContracts.Dtos;


namespace BlazorApp.Services.Interface;

public interface IProductInspectionHttpService
{
    //Task<ProductInspectionResultDto> ReviewProductAsync(ProductInspectionRequestDto dto);
    Task<ProductInspectionResultDto> VerifyProductAsync(int productId, ProductVerificationDto dto);
}
