using ApiContracts.Dtos;

namespace BlazorApp.Services.Interface;

public interface ICustomerInspectionService
{
    public Task<EnableSellDto> SetCanSellAsync(int customerId, EnableSellDto dto);
}
