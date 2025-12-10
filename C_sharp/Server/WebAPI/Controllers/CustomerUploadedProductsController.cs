// using ApiContracts.Dtos;
// using Microsoft.AspNetCore.Http;
// using Microsoft.AspNetCore.Mvc;
// using ReSpawnMarket.SDK.ServiceInterfaces;

// namespace WebAPI.Controllers
// {
//     [Route("api/uploaded-product")]
//     [ApiController]
//     public class CustomerUploadedProductsController : ControllerBase
//     {
//         private readonly IGetProductService getProductService;
//         public CustomerUploadedProductsController(IGetProductService getProductService)
//         {
//             this.getProductService=getProductService;
//         }
//       [HttpGet("{customerId:int}/products")]
//     [ProducesResponseType(typeof(IEnumerable<CustomerProductStatusDto>), StatusCodes.Status200OK)]
//     public async Task<IActionResult> GetCustomerProductsAsync(int customerId, CancellationToken ct)
//     {
//         // HERE you call your Java gRPC/service that already fetches
//         // products for a seller/customer. I’ll show the mapping conceptually.

//         var grpcResponse = await getProductService.GetByCustomerAsync(customerId, ct);
//         // ^ use whatever existing SDK method you already have that returns your products

//         var result = grpcResponse.Products.Select(p => new CustomerProductStatusDto
//         {
//             ProductId      = p.Id,
//             Name           = p.Name,
//             Price          = (decimal)p.Price,
//             ApprovalStatus = ToDtoApprovalStatus(p.ApprovalStatus),
//             PawnshopId     = p.PawnshopId,
//             PawnshopName   = p.PawnshopName,
//             Comments       = p.Comments  
//         });

//         return Ok(result);
//     }

//     private ApiContracts.Dtos.Enums.ApprovalStatus ToDtoApprovalStatus(Com.Respawnmarket.ApprovalStatus status)
//     {
//         return status switch
//         {
//             Com.Respawnmarket.ApprovalStatus.Approved  => ApiContracts.Dtos.Enums.ApprovalStatus.APPROVED,
//             Com.Respawnmarket.ApprovalStatus.Reviewing => ApiContracts.Dtos.Enums.ApprovalStatus.REVIEWING,
//             Com.Respawnmarket.ApprovalStatus.Rejected  => ApiContracts.Dtos.Enums.ApprovalStatus.REJECTED,
//             Com.Respawnmarket.ApprovalStatus.Pending   => ApiContracts.Dtos.Enums.ApprovalStatus.PENDING,
//             _ => throw new NotImplementedException()
//         };
//     }
// }
