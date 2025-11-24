using System;
using ApiContracts;
using ApiContracts.Dtos;

namespace BlazorApp.InterfaceServices;

public interface IPurcharseService
{
    public Task<BuyProductsResultDto> BuyProductsAsync(BuyProductRequestDto request);

}
