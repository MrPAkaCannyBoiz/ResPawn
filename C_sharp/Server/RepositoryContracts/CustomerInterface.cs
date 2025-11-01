using System;
using Entities;

namespace RepositoryContracts;

public interface CustomerInterface
{
    public Task<Customer> AddAsync(Customer customer);

    public Task<Customer> GetSingleAsync(int id);
}
