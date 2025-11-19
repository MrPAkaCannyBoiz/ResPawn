using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace ReSpawnMarket.Shared;

public class ProductInspectionRequestDto
{
    public int ProductId { get; set; }
    public int ResellerId { get; set; }
    public string Status { get; set; }
    public string? Comment { get; set; }

    public ProductInspectionRequestDto(int productId, int resellerId, string status, string? comment)
    {
        ProductId = productId;
        ResellerId = resellerId;
        Status = status;
        Comment = comment;
    }
}
