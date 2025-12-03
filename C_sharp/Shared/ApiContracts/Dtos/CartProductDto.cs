using System;

namespace ApiContracts.Dtos;

public class CartProductDto
{
    public required int CartId {get;set;}
    public required int ProductId {get;set;}
    public required int Quantity {get;set;}
}
