using System;
using ApiContracts.Dtos;

namespace BlazorApp.Services;

public interface ICustomerServices
{
    public Task<CustomerDto> AddCustomerAsync(CreateCustomerDto request);
    public Task<CustomerDto> GetSingleAsync(int id);
}
