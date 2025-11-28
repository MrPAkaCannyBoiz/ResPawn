using ApiContracts.Dtos;

namespace BlazorApp.Services;

public interface IUpdateCustomerService
{
    public Task<CustomerDto> UpdateCustomerAsync(int id, UpdateCustomerDto request);
}
