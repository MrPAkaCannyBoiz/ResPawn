using System;
using System.Collections.Generic;
using System.Text;

namespace ApiContracts.Dtos
{
    public class CreditCardDto
    {
        public string CardNumber { get; set; } = "";
        public string CardholderName { get; set; } = "";
        public string Expiration { get; set; } = "";
        public string CVV { get; set; } = "";
    }
}
