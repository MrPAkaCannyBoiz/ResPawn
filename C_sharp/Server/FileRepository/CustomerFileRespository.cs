using System;
using System.Text.Json;
using Entities;
using RepositoryContracts;

namespace FileRepository;

public class CustomerFileRespository : CustomerInterface
{

    private readonly string filePath = "Customer.json";

    public CustomerFileRespository()
    {
        if(!File.Exists(filePath) || string.IsNullOrWhiteSpace(File.ReadAllText(filePath)))
        {
            File.WriteAllText(filePath, "[]");
        }
    }
    public async Task<Customer> AddAsync(Customer customer)
    {
        string customerAsJson = await File.ReadAllTextAsync(filePath);
        List<Customer>? customers;
        try
        {
            customers = JsonSerializer.Deserialize<List<Customer>>(customerAsJson)!;
        }
        catch
        {
            customers = new List<Customer>();
        }
        if (customers == null)
        {
            customers = new List<Customer>();
        }
        int maxId = customers.Count > 0 ? customers.Max(c => c.Id) : 0;
        customer.Id = maxId + 1;
        customers.Add(customer);
        customerAsJson = JsonSerializer.Serialize(customers);
        await File.WriteAllTextAsync(filePath, customerAsJson);
        return customer;
    }

    public async Task<Customer> GetSingleAsync(int id)
    {
        string customerAsJson = await File.ReadAllTextAsync(filePath);
        List<Customer> customers = JsonSerializer.Deserialize<List<Customer>>(customerAsJson)!;
         Customer? customer = customers.FirstOrDefault(c => c.Id == id);
        return customer!;
    }
}
