using ApiContracts.Dtos.Enums;
using System;
using System.Collections.Generic;
using System.Text;

namespace ApiContracts.Dtos;

public class UploadProductDto
{
   
    public required double Price { get; set; }
    public required string Condition { get; set; }
    public required string Description { get; set; } = "";
    public required string Name { get; set; }
    public Category Category { get; set; }
    public string? OtherCategory { get; set; }
    public required List<string> ImageUrls { get; set; }

}
