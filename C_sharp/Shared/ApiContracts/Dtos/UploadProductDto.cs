using ApiContracts.Dtos.Enums;
using System;
using System.Collections.Generic;
using System.Text;

namespace ApiContracts.Dtos;

public class UploadProductDto
{
    /*
    //proto message of this is:
    message UploadProductRequest
        {
           double price = 1;
           string condition = 2;
           string name = 3;
           string photo_url = 4;
           Category category = 5;
           string description = 6;
           string other_category = 7;
           int32 sold_by_customer_id = 8;       
        }
    therefore, the properties must match these names for easier binding on Blazor client side except customer id 
    which is passed in URL
    */

    public required double Price { get; set; }
    public required string Condition { get; set; }
    public required string Description { get; set; } = "";
    public required string Name { get; set; }
    public required string PhotoUrl { get; set; }
    public Category Category { get; set; }
    public string? OtherCategory { get; set; }

}
