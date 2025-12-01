using ApiContracts.Dtos.Enums;
using System;
using System.Collections.Generic;
using System.Text;

namespace ApiContracts.Dtos;

public class ProductInspectionResultDto
{
    public int ProductId { get; set; }
    public ApprovalStatus ApprovalStatus { get; set; }
    public int PawnshopId { get; set; }
}
