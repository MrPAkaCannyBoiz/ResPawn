using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

using Com.Respawnmarket;
using Grpc.Core;
using ReSpawnMarket.SDK.ServiceInterfaces;
using ReSpawnMarket.Shared.ApiContracts.Dtos;

namespace ReSpawnMarket.SDK.Services;

public class ProductInspectionGrpcService : IProductInspectionService
{
    private readonly ProductInspectionService.ProductInspectionServiceClient client;

    public ProductInspectionGrpcService(ProductInspectionService.ProductInspectionServiceClient grpcClient)
    {
        client = grpcClient;
    }

    public async Task<ICollection<ProductDto>> GetPendingProductsAsync()
    {
        var request = new GetPendingProductsRequest
        {
            Empty = new Empty()
        };

        var response = await client.GetPendingProductsAsync(request);

        return response.Products.Select(p => new ProductDto(
            p.Id, p.Price, p.Sold, p.Condition,
            p.ApprovalStatus.ToString(), p.Name, p.PhotoUrl,
            p.Category.ToString(), p.SoldByCustomerId,
            p.Description, p.RegisterDate.ToDateTime().Date
        )).ToList();
    }

    public async Task<ProductInspectionResultDto> ReviewProductAsync(ProductInspectionRequestDto dto)
    {
        var request = new ProductInspectionRequest
        {
            ProductId = dto.ProductId,
            ResellerId = dto.ResellerId,
            Status = dto.Status,
            Comment = dto.Comment ?? ""
        };

        var response = await client.ReviewProductAsync(request);

        return new ProductInspectionResultDto(
            response.ProductId,
            response.Status,
            response.Comment
        );
    }
}
