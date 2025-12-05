using System;
using ApiContracts.Dtos;

namespace BlazorApp.Services.Interface;

public interface IRegisterCustomerService
{
    public Task<CustomerDto> AddCustomerAsync(CreateCustomerDto request);
}
