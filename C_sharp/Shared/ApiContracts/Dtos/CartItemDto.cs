using System;

namespace ApiContracts.Dtos;

public class CartItemDto
{
    public required int ProductId {get;set;}
    public required int Quantity {get;set;}

}
