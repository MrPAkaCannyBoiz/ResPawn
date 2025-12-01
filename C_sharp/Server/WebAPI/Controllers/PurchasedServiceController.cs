using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using ReSpawnMarket.SDK.ServiceInterfaces;
using ApiContracts.Dtos.Enums;
using ApiContracts.Dtos;
using Com.Respawnmarket;
using ApiContracts;
using System.Runtime.Serialization;
using Grpc.Core;

namespace WebAPI.Controllers
{
    [Route("api/purchases")]
    [ApiController]
    public class PurchasedServiceController : ControllerBase
    {
        private readonly IPurchaseService purchaseService;
        public PurchasedServiceController(IPurchaseService purchaseService)
        {
            this.purchaseService = purchaseService;
        }
        [HttpPost]
    [ProducesResponseType(typeof(BuyProductsResultDto), StatusCodes.Status200OK)]
    [ProducesResponseType(typeof(ProblemDetails), StatusCodes.Status400BadRequest)]
    [ProducesResponseType(typeof(ProblemDetails), StatusCodes.Status502BadGateway)]
    public async Task<IActionResult> BuyProductsAsync([FromBody] BuyProductRequestDto dto, CancellationToken ct)
        {
            if(!ModelState.IsValid) return ValidationProblem(ModelState);
Console.WriteLine(
        $"[WebAPI] BuyProducts: CustomerId={dto.CustomerId}, " +
        $"ItemsCount={dto.Items?.Count ?? 0}, " +
        $"FirstItemProductId={dto.Items?.FirstOrDefault()?.ProductId}, " +
        $"FirstItemQuantity={dto.Items?.FirstOrDefault()?.Quantity}");
            //Map API DTO -> gRPC request
            var grpcReq = new BuyProductsRequest
            {
                CustomerId = dto.CustomerId
            };
            grpcReq.Items.Add(dto.Items.Select(i=> new CartItem
            {
                ProductId = i.ProductId,
                Quantity = i.Quantity
            }));
            BuyProductsResponse grpcRes;
            try
            {
                grpcRes = await purchaseService.BuyProductsAsync(grpcReq, ct);
            }
            catch(RpcException ex)
            {
                return StatusCode(StatusCodes.Status502BadGateway, ex.Status.Detail);
            }
            if (grpcRes.Transaction is null || grpcRes.ShoppingCart is null)
            {
                return StatusCode(StatusCodes.Status502BadGateway, "Upstream gRPC service returned an incomplete response");
            }
        
        
            // Map gRpc to dto
            var TxDto = new TransactionDto{
                Id = grpcRes.Transaction.Id,
             Date = grpcRes.Transaction.Date.ToDateTime(),
             ShoppingCartId = grpcRes.Transaction.ShoppingCartId, // or ShoppingCartId if you rename it
             CustomerId = grpcRes.Transaction.CustomerId
        };
        //grpc shoppincCartDto
            var CartDto = new ShoppinCartDto
            {
                Id =grpcRes.ShoppingCart.Id,
                TotalPrice = grpcRes.ShoppingCart.TotalPrice
            };
            var CartProductDtos = grpcRes.CartProducts
            .Select(cp => new CartProductDto
            {
                CartId = cp.CartId,
                ProductId = cp.ProductId,
                Quantity = cp.Quantity
            })
            .ToList();
            
            var productDtos = grpcRes.PurchasedProducts
            .Select(p => new ProductDto
            {
                Id = p.Id,
                Price = p.Price,
                Sold = p.Sold,
                Condition = p.Condition,
                ApprovalStatus = p.ApprovalStatus.ToString(),
                Name = p.Name,
                PhotoUrl = p.PhotoUrl,
                Category = p.Category.ToString(),
                Description = p.Description,
                SoldByCustomerId = p.SoldByCustomerId,
                RegisterDate = p.RegisterDate.ToDateTime()
            })
            .ToList();

        // Wrap everything in one result DTO
        var result = new BuyProductsResultDto
        {
            Transaction = TxDto,
            ShoppingCart = CartDto,
            CartProducts = CartProductDtos,
            PurchasedProduct = productDtos
        };
        return Ok(result);
    }

}
}
