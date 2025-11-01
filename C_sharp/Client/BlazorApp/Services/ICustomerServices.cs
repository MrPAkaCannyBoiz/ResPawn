using System;
using ApiContracts;

namespace BlazorApp.Services;

public interface ICustomerServices
{
    public Task<CustomerDto> AddCustomerAsync(CreateCustomerDto request);
    public Task<CustomerDto> GetSingleAsync(int id);
}
