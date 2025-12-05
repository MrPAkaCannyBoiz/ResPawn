using System;
using ApiContracts.Dtos;

namespace BlazorApp.Services.Interface;

public interface IPurchaseService
{
    public Task<BuyProductsResultDto> BuyProductsAsync(BuyProductRequestDto request);

}
