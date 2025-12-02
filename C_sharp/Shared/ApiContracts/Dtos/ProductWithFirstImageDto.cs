using System;
using System.Collections.Generic;
using System.Text;

namespace ApiContracts.Dtos;

public class ProductWithFirstImageDto
{
    public required int Id { get; set; }
    public required double Price { get; set; }
    public required bool Sold { get; set; }
    public required string Condition { get; set; }
    public string ApprovalStatus { get; set; } = string.Empty;
    public required string Name { get; set; }
    public string Category { get; set; } = string.Empty;
    public required string Description { get; set; }
    public required int SoldByCustomerId { get; set; }
    public required DateTime RegisterDate { get; set; }
    public required string OtherCategory { get; set; }
    public required ImageDto Image { get; set; }
}
