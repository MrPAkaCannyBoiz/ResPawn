using System;
using System.Collections.Generic;
using System.Text;

namespace ApiContracts.Dtos;

public class ImageDto
{
    public required int Id { get; set; }
    public required string Url { get; set; }
    public required int ProductId { get; set; }
}
