using ApiContracts.Dtos.Enums;
using System;
using System.Collections.Generic;
using System.Text;

namespace ApiContracts.Dtos;

public class ProductDto
{
    // in proto file:
    //int32 id = 1;
    //double price = 2;
    //bool sold = 3;
    //string condition = 4;
    //ApprovalStatus approval_status = 5;
    //string name = 6;
    //string photo_url = 7;
    //Category category = 8;
    //string description = 9;
    //int32 sold_by_customer_id = 10;
    //google.protobuf.Timestamp register_date = 11;

    public required int Id { get; set; }
    public required double Price { get; set; }
    public required bool Sold { get; set; }
    public required string Condition { get; set; }
    public string ApprovalStatus { get; set; } = string.Empty;
    public required string Name { get; set; }
    public required string PhotoUrl { get; set; }
    public string Category { get; set; } = string.Empty;
    public required string Description { get; set; }
    public required int SoldByCustomerId { get; set; }
    public required DateTime RegisterDate { get; set; }
}
