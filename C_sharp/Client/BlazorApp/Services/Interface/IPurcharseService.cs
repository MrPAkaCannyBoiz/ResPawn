using System;
using ApiContracts;
using ApiContracts.Dtos;

namespace BlazorApp.Services.Interface;

public interface IPurcharseService
{
    public Task<BuyProductsResultDto> BuyProductsAsync(BuyProductRequestDto request);

}
