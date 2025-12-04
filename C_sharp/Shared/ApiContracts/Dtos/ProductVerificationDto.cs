using System;
using System.Collections.Generic;
using System.Text;

namespace ApiContracts.Dtos;

public class ProductVerificationDto
{
    public int ResellerId { get; set; }
    public bool IsAccepted { get; set; }
    public string Comments { get; set; } = "";
}
