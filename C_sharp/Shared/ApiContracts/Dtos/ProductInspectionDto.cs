using System;
using System.Collections.Generic;
using System.Text;

namespace ApiContracts.Dtos;

public class ProductInspectionDto
{
    public int ResellerId { get; set; }
    public string Comments { get; set; } = string.Empty;
    public bool IsAccepted { get; set; }
    public int PawnshopId { get; set; }
}
