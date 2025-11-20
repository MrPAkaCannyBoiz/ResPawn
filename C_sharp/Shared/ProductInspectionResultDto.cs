using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace C_sharp.Shared
{

public class ProductInspectionResultDto
{
    public int ProductId { get; set; }
    public string Status { get; set; }
    public string Comment { get; set; }

    public ProductInspectionResultDto(int productId, string status, string comment)
    {
        ProductId = productId;
        Status = status;
        Comment = comment;
    }
}

}