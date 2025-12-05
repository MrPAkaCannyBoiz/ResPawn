using ApiContracts.Dtos;

namespace BlazorApp.Services.Interface;

public interface IGetCustomerService
{
    public Task<CustomerDto> GetCustomerByIdAsync(int customerId);
    public Task<List<CustomerDto>> GetAllCustomersAsync();
}
