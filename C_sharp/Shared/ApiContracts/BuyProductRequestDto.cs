using System;
using ApiContracts.Dtos;

namespace ApiContracts;

public class BuyProductRequestDto
{
        
        public required int CustomerId {get;set;}
        public required ICollection<CartItemDto> Items {get;set;} 

}
