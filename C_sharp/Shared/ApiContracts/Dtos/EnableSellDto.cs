using System;
using System.Collections.Generic;
using System.Text;

namespace ApiContracts.Dtos;

public class EnableSellDto
{
    public int CustomerId { get; set; }
    public bool CanSell { get; set; }
}
