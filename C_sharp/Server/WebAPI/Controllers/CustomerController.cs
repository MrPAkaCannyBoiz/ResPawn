using ApiContracts;
using Entities;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using RepositoryContracts;

namespace WebAPI.Controllers
{
    [Route("[controller]")]
    [ApiController]
    public class CustomerController : ControllerBase
    {
        private readonly CustomerInterface customerInterface;

        public CustomerController(CustomerInterface customerInterface)
        {
            this.customerInterface = customerInterface;
        }

        [HttpPost]
        public async Task<ActionResult<CustomerDto>> AddCustomer([FromBody] CreateCustomerDto request)
        {
            var customer = new Customer { UserName = request.UserName, Password = request.Password };
            var created = await customerInterface.AddAsync(customer);

            if (created.UserName is null)
                return Problem("User was created with null Username. ", statusCode: 500);

            var result = new CustomerDto { Id = created.Id, UserName = created.UserName };
            return CreatedAtAction(nameof(GetById), new { Id = result.Id }, result);
        }

        [HttpGet("{id:int}")]    
        public async Task<ActionResult<CustomerDto>> GetById(int id)
        {
            Customer? c;
            try
            {
                c = await customerInterface.GetSingleAsync(id);
            }
            catch(KeyNotFoundException)
            {
                return NotFound();
            }
            if (c is null) return NotFound();

            return new CustomerDto
            {
                Id = c.Id,
                UserName = c.UserName ?? string.Empty
            };
        }
    }
}
