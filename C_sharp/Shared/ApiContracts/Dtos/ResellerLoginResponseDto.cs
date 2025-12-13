using System;

namespace ApiContracts;

public class ResellerLoginResponseDto
{
        public required int Id {get;set;}
        
        public required string Username {get;set;}
        public string Jwt { get; set; }
}
