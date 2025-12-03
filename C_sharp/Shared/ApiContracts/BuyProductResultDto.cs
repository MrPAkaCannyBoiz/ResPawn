using System;
using ApiContracts.Dtos;

namespace ApiContracts;

public class BuyProductsResultDto
{
    public required TransactionDto Transaction {get;set;}
    public required ShoppinCartDto ShoppingCart {get;set;}
    public required ICollection<CartProductDto> CartProducts {get;set;}
    public required ICollection<ProductDto> PurchasedProduct {get;set;}
}
