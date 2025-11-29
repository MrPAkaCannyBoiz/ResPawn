using ApiContracts.Dtos;

namespace BlazorApp.Services.Interface;

public interface IUpdateCustomerService
{
    public Task<CustomerDto> UpdateCustomerAsync(int id, UpdateCustomerDto request);
}
