using System;
using System.Data.Common;
using System.Diagnostics.Contracts;

namespace ApiContracts.Dtos;

public class ShoppingCartDto
{
    public required int Id {get;set;}
    public required double TotalPrice {get;set;}
}
