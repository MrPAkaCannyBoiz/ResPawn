using System;

namespace ApiContracts.Dtos;

public class BuyProductRequestDto
{
        
        public required int CustomerId {get;set;}
        public required ICollection<CartItemDto> Items {get;set;} 

}
