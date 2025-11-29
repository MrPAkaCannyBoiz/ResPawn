using System;
using ApiContracts.Dtos;

namespace BlazorApp.Services.Interface;

public interface ICustomerServices
{
    public Task<CustomerDto> AddCustomerAsync(CreateCustomerDto request);
    public Task<CustomerDto> GetSingleAsync(int id);
}
